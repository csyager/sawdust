use std::net::TcpListener;
use std::io::{self, prelude::*, BufReader};
use serde::Deserialize;
use reqwest::StatusCode;
use reqwest::blocking::Client;
use std::collections::HashMap;
use std::{env, fs};
use std::process;
use rustls::{ServerConfig, ServerConnection, StreamOwned};
use rustls::pki_types::{CertificateDer, PrivateKeyDer};
use std::sync::Arc;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum Error {
    #[error("IO error")]
    Disconnect(#[from] io::Error),
    #[error("the data for key `{0}` is not available")]
    JsonError(#[from] reqwest::Error),
    #[error("Failed to register with control plane, received response status {0}")]
    RegistrationError(String),
    #[error("unknown data store error")]
    Unknown,
}

fn read_http_request<R: std::io::Read>(reader: &mut BufReader<R>) -> Result<(String, Option<String>), Error> {
    // get HTTP headers
    let mut headers = String::new();
    loop {
        let mut line = String::new();
        let bytes_read = reader.read_line(&mut line)?;
        if bytes_read == 0 {
            break; // End of stream
        }
        headers.push_str(&line);
        if line == "\r\n" {
            // End of headers
            break;
        }
    }
    println!("Read headers: {}", headers);
    // Parse Content-Length if present
    let mut body = None;
    if let Some(content_length) = headers
        .lines()
        .find(|line| line.to_lowercase().starts_with("content-length:"))
        .and_then(|line| line.split(':').nth(1))
        .and_then(|len| len.trim().parse::<usize>().ok())
    {
        // Read the specified number of bytes for the body
        let mut body_buffer = vec![0; content_length];
        reader.read_exact(&mut body_buffer)?;
        body = Some(String::from_utf8_lossy(&body_buffer).to_string());
    }

    Ok((headers, body))
}

#[derive(Debug, Deserialize)]
struct RegisterResponse {
    #[serde(rename = "computeId")]
    compute_id: String,
    #[serde(rename = "workflowName")]
    workflow_name: String
}

fn register_with_control_plane(cp_endpoint: &str, workflow_id: &str, workflow_token: &str, certificate_string: &str) -> Result<String, Error> {
    let client = Client::new();
    let mut map = HashMap::new();
    map.insert("workflowId", workflow_id);
    map.insert("workflowToken", workflow_token);
    map.insert("certificate", certificate_string);
    let res = client.post(format!("{cp_endpoint}/register/compute"))
        .json(&map)
        .send()?;
    match res.status() {
        StatusCode::CREATED => {
            let body = res.json::<RegisterResponse>()?;
            println!("Successfully registered with sawdust control plane.  Compute ID = {}", body.compute_id);
            Ok(body.compute_id)
        },
        s => {
            Err(Error::RegistrationError(s.to_string()))
        }
    }

}

struct Config {
    endpoint: String,
    workflow_id: String,
    workflow_token: String,
    certificate_path: String,
    listener_port: String
}

impl Config {
    fn build(args: &[String]) -> Result<Config, &'static str> {
        if args.len() < 5 {
            return Err("Not enough arguments.");
        }
        let endpoint = args[1].clone();
        let workflow_id = args[2].clone();
        let workflow_token = args[3].clone();
        let certificate_path = args[4].clone();
        let listener_port = args[5].clone();

        Ok(Config {endpoint, workflow_id, workflow_token, certificate_path, listener_port})
    }
}

fn get_certificate_string(path: &str) -> Result<String, std::io::Error>{
    let message: String = fs::read_to_string(path)?;
    Ok(message)
}

// Load public certificate from file.
fn load_certs(filename: &str) -> io::Result<Vec<CertificateDer<'static>>> {
    // Open certificate file.
    let certfile = fs::File::open(filename)
        .map_err(|e| io::Error::new(io::ErrorKind::Other, format!("failed to open {}: {}", filename, e)))?;
    let mut reader = io::BufReader::new(certfile);

    // Load and return certificate.
    rustls_pemfile::certs(&mut reader).collect()
}

// Load private key from file.
fn load_private_key(filename: &str) -> io::Result<PrivateKeyDer<'static>> {
    // Open keyfile.
    let keyfile = fs::File::open(filename)
        .map_err(|e| io::Error::new(io::ErrorKind::Other, format!("failed to open {}: {}", filename, e)))?;
    let mut reader = io::BufReader::new(keyfile);

    // Load and return a single private key.
    rustls_pemfile::private_key(&mut reader).map(|key| key.unwrap())
}

fn load_tls_config() -> io::Result<ServerConfig>{
    // Load public certificate.
    let certs = load_certs("certificate.pem")?;
    // Load private key.
    let key = load_private_key("private_key.pem")?;

    let server_config = ServerConfig::builder()
        .with_no_client_auth()
        .with_single_cert(certs, key)
        .map_err(|_e| io::Error::new(io::ErrorKind::Other, "failed to build server config: {}"))?;
    Ok(server_config)
}

fn main() {
    // read cli args
    let args: Vec<String> = env::args().collect();
    let config = Config::build(&args).unwrap_or_else(|err| {
        eprintln!("Error parsing arguments: {err}");
        process::exit(1)
    });

    // read certificate string
    let certificate_string = get_certificate_string(&config.certificate_path).unwrap_or_else(|err| {
        eprintln!("Error reading certificate file: {err}");
        process::exit(1)
    });
    
    // load TLS config
    let tls_config = load_tls_config().unwrap_or_else(|err| {
        eprintln!("Failed to configure TLS: {}", err);
        process::exit(1);
    });

    // register with control plane
    let compute_id = register_with_control_plane(&config.endpoint, &config.workflow_id, &config.workflow_token, &certificate_string).unwrap_or_else(|err| {
        eprintln!("Register request failed: {}", err);
        process::exit(1);
    });

    // start listener
    let listener = TcpListener::bind(format!("127.0.0.1:{}", &config.listener_port)).unwrap_or_else(|err| {
        eprintln!("Failed to start listener: {}", err);
        process::exit(1);
    });
    println!("Listening on 127.0.0.1:9000");

    for stream in listener.incoming() {
        match stream {
            Ok(stream) => {
                println!("New connection: {:?}", stream.peer_addr().unwrap());
                let tls_connection = ServerConnection::new(Arc::new(tls_config.clone())).unwrap();
                let mut tls_stream = StreamOwned::new(tls_connection, stream);

                let mut reader = BufReader::new(&mut tls_stream);
                match read_http_request(&mut reader) {
                    Ok((headers, body)) => {
                        println!("Received headers:\n{}", headers);
                        if let Some(body) = body {
                            println!("Received body:\n{}", body);
                        } else {
                            println!("No body received");
                        }
                    },
                    Err(e) => eprintln!("Error reading request: {}", e),
                }
            }
            Err(e) => eprintln!("Connection failed: {}", e),
        }
    }
}
