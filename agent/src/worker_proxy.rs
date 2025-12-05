use std::net::TcpStream;
use std::io::{Read, Write};
use crate::activity::Activity;

use openssl::rsa::Rsa;
use openssl::pkey::PKey;
use openssl::sign::Signer;
use openssl::hash::MessageDigest;
use base64;
use serde_json;
use std::fs::File;

pub fn send_activity_to_worker(activity: Activity) {
    let worker_address = "127.0.0.1:9001";

    match TcpStream::connect(worker_address) {
        Ok(mut stream) => {
            println!("Connected to worker at {}", worker_address);
            match stream.write_all(serde_json::to_string(&activity).expect("JSON could not be converted to string.").as_bytes()) {
                Ok(_) => {
                    println!("Sent request to worker.");
                    let mut buffer = [0; 1024];
                    match stream.read(&mut buffer) {
                        Ok(bytes_read) if bytes_read > 0 => {
                            let response = String::from_utf8_lossy(&buffer[..bytes_read]);
                            println!("Server response: {}", response);
                        }
                        Ok(_) => {
                            println!("Server closed the connection.");
                        }
                        Err(e) => {
                            eprintln!("Failed to read response: {}", e);
                        }
                    }
                },
                Err(e) => eprintln!("Failed to send message: {}", e),
            }
        },
        Err(e) => eprintln!("Failed to connect: {}", e),
    }
}

fn load_private_key(filename: &str) -> Result<PKey<openssl::pkey::Private>, Box<dyn std::error::Error>> {
    let mut file = File::open(filename)?;
    let mut key_data = String::new();
    file.read_to_string(&mut key_data)?;
    
    let rsa = Rsa::private_key_from_pem(key_data.as_bytes())?;
    let pkey = PKey::from_rsa(rsa)?;
    
    Ok(pkey)
}

fn sign_message(message: &str, private_key: &PKey<openssl::pkey::Private>) -> Result<String, Box<dyn std::error::Error>> {
    let mut signer = Signer::new(MessageDigest::sha256(), private_key)?;
    signer.update(message.as_bytes())?;
    let signature = signer.sign_to_vec()?;
    
    // Convert to Base64 for transmission
    Ok(base64::encode(signature))
}

pub fn send_update_to_control_plane(activity: Activity) {
    let server_address = "127.0.0.1:8080";
    // sign the request with the agent's private key
    let private_key = match load_private_key("private_key.pem") {
        Ok(key) => key,
        Err(e) => {
            eprintln!("Failed to load private key: {}", e);
            return;
        }
    };

    // Convert activity to JSON
    let json_body = match serde_json::to_string(&activity) {
        Ok(json) => json,
        Err(e) => {
            eprintln!("Failed to serialize activity: {}", e);
            return;
        }
    };

    let signature = match sign_message(&json_body, &private_key) {
        Ok(sig) => sig,
        Err(e) => {
            eprintln!("Failed to sign request: {}", e);
            return;
        }
    };

    match TcpStream::connect(server_address) {
        Ok(mut stream) => {
            println!("Connected to control plane at {}", server_address);

            // send signed request (JSON + signature) to control plane
            let request_with_signature = format!(
                "PUT /activity/{} HTTP/1.1\r\n\
                Host: {}\r\n\
                Content-Type: application/json\r\n\
                Content-Length: {}\r\n\
                X-Signature: {}\r\n\
                \r\n\
                {}",
                activity.activity_id,
                server_address,
                json_body.len(),
                signature,
                json_body
            );

            match stream.write_all(request_with_signature.as_bytes()) {
                Ok(_) => {
                    println!("Sent request to control plane.");
                    let mut buffer = [0; 1024];
                    match stream.read(&mut buffer) {
                        Ok(bytes_read) if bytes_read > 0 => {
                            let response = String::from_utf8_lossy(&buffer[..bytes_read]);
                            println!("Server response: {}", response);
                        }
                        Ok(_) => {
                            println!("Server closed the connection.");
                        }
                        Err(e) => {
                            eprintln!("Failed to read response: {}", e);
                        }
                    }
                },
                Err(e) => eprintln!("Failed to send message: {}", e),
            }
        },
        Err(e) => eprintln!("Failed to connect: {}", e),
    }
}