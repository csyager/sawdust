use rustls::{ServerConfig, ServerConnection, StreamOwned};
use std::io::{prelude::*, BufReader};
use std::net::TcpStream;
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use std::io::Error;

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct Activity {
    activity_id: String,
    workflow_name: String,
    workflow_state: String,

}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct PushActivityRequest {
    compute_id: String,
    activity: Activity
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

pub fn parse_http_connection(stream: TcpStream, tls_config: &ServerConfig) {
    println!("New connection: {:?}", stream.peer_addr().unwrap());
    let tls_connection = ServerConnection::new(Arc::new(tls_config.clone())).unwrap();
    let mut tls_stream = StreamOwned::new(tls_connection, stream);

    let mut reader = BufReader::new(&mut tls_stream);
    match read_http_request(&mut reader) {
        Ok((headers, body)) => {
            println!("Received headers:\n{}", headers);
            if let Some(body) = body {
                println!("Received body:\n{}", body);
                let push_activity_request: PushActivityRequest = serde_json::from_str(&body).expect("JSON was not well-formatted");

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
        },
        Err(e) => eprintln!("Error reading request: {}", e),
    }
}