use serde::Deserialize;
use reqwest::StatusCode;
use reqwest::blocking::Client;
use std::collections::HashMap;
use std::io;
use thiserror::Error;

#[derive(Debug, Deserialize)]
struct RegisterResponse {
    #[serde(rename = "computeId")]
    compute_id: String,
    #[serde(rename = "workflowName")]
    workflow_name: String
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

pub fn register_with_control_plane(cp_endpoint: &str, workflow_id: &str, workflow_token: &str, certificate_string: &str) -> Result<String, Error> {
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