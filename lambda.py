import json
import boto3
import urllib
from botocore.vendored import requests
s3 = boto3.client('s3')
API = "https://686b4bca425b2b5a69a9a5ac923f1bf4.gr7.us-east-1.eks.amazonaws.com/"

def lambda_handler(event, context):
    #print("Received event: " + json.dumps(event, indent=2))

    # Get the object from the event an  d show its content type
    bucket = event['Records'][0]['s3']['bucket']['name']
    key = urllib.parse.unquote_plus(event['Records'][0]['s3']['object']['key'], encoding='utf-8')
    try:
        response = s3.get_object(Bucket=bucket, Key=key)
        data = json.dumps({
            'key' : key
        })
        requests.post(url=API,data=data)
        print("S3 object is "+key)
        #print("CONTENT TYPE: " + response['ContentType'])
        return json.dumps({
            "message" : "sucesss"
        })
    except Exception as e:
        print(e)
        print('Error getting object {} from bucket {}. Make sure they exist and your bucket is in the same region as this function.'.format(key, bucket))
        raise e
