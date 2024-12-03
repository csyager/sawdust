# sawdust-agent

## What this agent does
This agent enables a compute node to communicate with the sawdust control plane.  This includes:
1.  Sending a registration request to the control plane.  This request payload must include the secret key for the workflow being operated on, and shares credentials with the control plane that will be used to push work to the node.
2.  Starts a listener that accepts requests from the control plane.  These requests will include workflow metadata that the host should process.
3.  Calls worker process to perform work
4.  Sends result back to control plane.