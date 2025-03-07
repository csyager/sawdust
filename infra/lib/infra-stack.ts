import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import { DynamoDbInfra } from './constructs/dynamodb';

export class InfraStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpc = new ec2.Vpc(this, 'SawdustControlPlaneVpc', {});

    new DynamoDbInfra(this, `${id}-dynamodb-infra`, props);
  }
}
