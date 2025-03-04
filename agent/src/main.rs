use std::net::TcpListener;

use std::{env, fs};
use std::process;
use rustls::ServerConfig;
use rustls::pki_types::{CertificateDer, PrivateKeyDer};
use thiserror::Error;
use std::io;

mod registration;
mod activity;

// command line parameters
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

// error types
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

fn get_certificate_string(path: &str) -> Result<String, std::io::Error>{
    println!("path: {}", path);
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
    // TODO:  we should ensure that incoming requests use the correct compute ID
    let compute_id = registration::register_with_control_plane(&config.endpoint, &config.workflow_id, &config.workflow_token, &certificate_string).unwrap_or_else(|err| {
        eprintln!("Register request failed: {}", err);
        process::exit(1);
    });
    println!("compute_id = {}", compute_id);

    // start listener
    let listener = TcpListener::bind(format!("127.0.0.1:{}", &config.listener_port)).unwrap_or_else(|err| {
        eprintln!("Failed to start listener: {}", err);
        process::exit(1);
    });
    println!("Listening on 127.0.0.1:9000");

    for stream in listener.incoming() {
        match stream {
            Ok(stream) => {
                activity::parse_http_connection(stream, &tls_config);
            }
            Err(e) => eprintln!("Connection failed: {}", e),
        }
    }
}
