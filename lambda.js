const https = require('https');

const doPostRequest = (key) => {

  const data = {"appId": "6450ec5c-551e-11ec-bf63-0242ac130002", "key":${key}};

  return new Promise((resolve, reject) => {
    const options = {
      host: 'ec2-44-200-16-152.compute-1.amazonaws.com',
      path: '',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    };

    //create the request object with the callback with the result
    const req = https.request(options, (res) => {
      resolve(JSON.stringify(res.statusCode));
    });

    // handle the possible errors
    req.on('error', (e) => {
      reject(e.message);
    });

    //do the request
    req.write(data);

    //finish the request
    req.end();
  });
};


exports.handler = async (event) => {
  console.log(event.Records.s3)
  await doPostRequest(event)
    .then(result => console.log(Status code: ${result}))
    .catch(err => console.error(Error doing the request for the event: ${JSON.stringify(event)} => ${err}));
};
