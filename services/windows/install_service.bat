"C:\Program Files\Java\commons-daemon-1.1.0-bin-windows\amd64\prunsrv.exe" //IS//cleverforwarder^
 --DisplayName "Clever Avl Forwarder"^
 --Description "An agent that forwards Clever SQL data to an SQS Queue"^
 --Startup=auto^
 --Install="C:\Program Files\Java\commons-daemon-1.1.0-bin-windows\amd64\prunsrv.exe"^
 --Jvm="C:\Program Files\Java\jre1.8.0_211\bin\server\jvm.dll" ^
 --Classpath "C:\clever-avl-forwarder\clever-avl-forwarder.jar"^
 --Environment="PATH=C:\Program Files\Java\jre1.8.0_211\bin"^
 --JavaHome="C:\Program Files\Java\jre1.8.0_211"^
 --StartPath=C:\clever-avl-forwarder\^
 --JvmOptions="-XX:+HeapDumpOnOutOfMemoryError"^
 --StartMode=jvm^
 --StartClass=org.onebusaway.forwarder.CleverAvlForwarderMain^
 --StopMode=jvm^
 --StopClass=org.onebusaway.forwarder.CleverAvlForwarderMain^
 --StopMethod=stop^
 --StdOutput=C:\clever-avl-forwarder\logs\forwarder-stdout.log^
 --StdError=C:\clever-avl-forwarder\logs\forwarder-sterr.log