use rustls::{ServerConfig, ServerConnection, StreamOwned};
use std::io::{prelude::*, BufReader};
use std::net::TcpStream;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use std::io::Error;

use crate::worker_proxy::{send_activity_to_worker, send_update_to_control_plane};

const PUSH_ACTIVITY_PATH: &str = "/activity";
const UPDATE_ACTIVITY_PATH: &str = "/update";

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Activity {
    pub activity_id: String,
    workflow_id: String,
    workflow_state: String,
    activity_state: String
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct PushActivityRequest {
    compute_id: String,
    activity: Activity
}


fn read_http_request<R: std::io::Read>(reader: &mut BufReader<R>) -> Result<(String, Option<String>, Option<String>), Error> {
    // get HTTP headers and path
    let mut headers = String::new();
    let mut first_line = String::new();

    // read the first line to get the http path
    let bytes_read = reader.read_line(&mut first_line)?;
    if bytes_read == 0 {
        return Ok((String::new(), None, None));
    }

    let path = first_line
        .split_whitespace()
        .nth(1)
        .map(String::from);
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

    Ok((headers, path, body))
}

pub fn parse_http_connection(stream: TcpStream, tls_config: &ServerConfig) {
    println!("New connection: {:?}", stream.peer_addr().unwrap());
    let tls_connection = ServerConnection::new(Arc::new(tls_config.clone())).unwrap();
    let mut tls_stream = StreamOwned::new(tls_connection, stream);

    let mut reader = BufReader::new(&mut tls_stream);
    match read_http_request(&mut reader) {
        Ok((headers, path, body)) => {
            println!("Received request for path {:?}", path);
            println!("Received headers:\n{}", headers);

            // path handling
            if path.as_deref().unwrap_or("") == PUSH_ACTIVITY_PATH {
                if let Some(body) = body {
                    println!("Received body:\n{}", body);
                    let push_activity_request: PushActivityRequest = serde_json::from_str(&body).expect("JSON was not well-formatted");
                    send_activity_to_worker(push_activity_request.activity);
                } else {
                    println!("No body received");
                }
                
                let response = "HTTP/1.1 200 OK\r\n\
                    Content-Type: text/plain\r\n\
                    Content-Length: 2\r\n\
                    Connection: close\r\n\
                    \r\n\
                    OK";
    
                match tls_stream.write_all(response.as_bytes()) {
                    Ok(_) => println!("Successfully sent {} bytes to control plane.", response.len()),
                    Err(e) => eprintln!("Failed to send response: {}", e)
                }
            } else if path.as_deref().unwrap_or("") == UPDATE_ACTIVITY_PATH {
                if let Some(body) = body {
                    println!("Received body:\n{}", body);
                    let push_activity_request: PushActivityRequest = serde_json::from_str(&body).expect("JSON was not well-formatted");
                    send_update_to_control_plane(push_activity_request.activity);
                } else {
                    println!("No body received");
                }
            }

            
        },
        Err(e) => eprintln!("Error reading request: {}", e),
    }
}
