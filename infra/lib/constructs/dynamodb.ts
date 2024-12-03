import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ddb from 'aws-cdk-lib/aws-dynamodb'

export class DynamoDbInfra extends Construct {
    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id);
        const table = new ddb.Table(this, 'Table', {
          tableName: "workflows-table",
          partitionKey: {
            name: 'workflowId',
            type: ddb.AttributeType.STRING,
          },
          billingMode: ddb.BillingMode.PAY_PER_REQUEST,
        });
    }
}