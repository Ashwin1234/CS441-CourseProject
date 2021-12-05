#Introduction
First we need to understand certain concepts

## Cloudflow Streamlets
Streamlets are cannonical class names which have inlets and outlets. A Streamlet can have more than one inlet and outlet.
There are different streamlet shapes
+ Ingress - Ingress are streamlets with zero inlets and one or more outlets 
+ Processor - A processor has one inlet and one outlet.
+ Fanout - FanOut-shaped streamlets have a single inlet and two or more outlets.
+ Fanin - FanIn-shaped streamlets have a single outlet and two or more inlets.
+ Egress - Egress has inlets but zero outlets.

## Avro Schemas
Inlets and outlets of specic streamlets can handle data specified by Avro schemas.

## Blueprint
The shape of Streamlets with inlets and outlets are specified in a blueprint.

The image below shows a general understanding of cloudflow model.
![image](./images/cloudflow_image.png)


