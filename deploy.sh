#!/bin/bash
sshpass -p "dsv2016" scp -r ../dsv dsv@192.168.0.1:

sed -i 's/hostname = "192.168.0.1"/hostname = "192.168.0.2"/g' src/main/resources/application.conf
sed -i 's/node-id = "node-1"/node-id = "node-2"/g' src/main/resources/application.conf
sshpass -p "dsv2016" scp -r ../dsv dsv@192.168.0.2:

sed -i 's/hostname = "192.168.0.2"/hostname = "192.168.0.3"/g' src/main/resources/application.conf
sed -i 's/node-id = "node-2"/node-id = "node-3"/g' src/main/resources/application.conf
sshpass -p "dsv2016" scp -r ../dsv dsv@192.168.0.3:

sed -i 's/hostname = "192.168.0.3"/hostname = "192.168.0.4"/g' src/main/resources/application.conf
sed -i 's/node-id = "node-3"/node-id = "node-4"/g' src/main/resources/application.conf
sed -i 's/parent = "192.168.0.1"/parent = "192.168.0.3"/g' src/main/resources/application.conf
sed -i 's/parent-id = "node-1"/parent-id = "node-3"/g' src/main/resources/application.conf
sshpass -p "dsv2016" scp -r ../dsv dsv@192.168.0.4:

sed -i 's/hostname = "192.168.0.4"/hostname = "192.168.0.5"/g' src/main/resources/application.conf
sed -i 's/node-id = "node-4"/node-id = "node-5"/g' src/main/resources/application.conf
sshpass -p "dsv2016" scp -r ../dsv dsv@192.168.0.5:

sed -i 's/hostname = "192.168.0.5"/hostname = "192.168.0.1"/g' src/main/resources/application.conf
sed -i 's/node-id = "node-5"/node-id = "node-1"/g' src/main/resources/application.conf
sed -i 's/parent = "192.168.0.3"/parent = "192.168.0.1"/g' src/main/resources/application.conf
sed -i 's/parent-id = "node-3"/parent-id = "node-1"/g' src/main/resources/application.conf
