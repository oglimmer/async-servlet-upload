This simulates a slow client and how an asynchronous servlet processing make things better
===========================================================================================

how to run the upload test
--------------------------

1.) start web server

mvn jetty:run

2.) start an hostile client and we assume our upload servlet is synchronous

./run.sh UploadServletSync 0 250 500

3.) wait for ~1 minute

4.) try to access http://localhost:8080 (e.g. curl -v http://localhost:8080) and you'll see the web server is dead

5.) kill the hostile client

kill -9 $(ps -ef|grep de.oglimmer.client.Startup|grep -v "grep"|awk '{print $2}')

6.) access http://localhost:8080 (e.g. curl -v http://localhost:8080) and you'll see the web server is back in business

7.) start another hostile client, this time we assume the upload server does async processing: 

./run.sh UploadServletAsync 0 250 500

8.) access http://localhost:8080 (e.g. curl -v http://localhost:8080) as the attacker can't consume all threads.


how to run the post test
--------------------------

Similar to upload test, just use `PostServletAsync` or `PostServletSync`

what about sending the header data very slow?
---------------------------------------------

The class the de.oglimmer.client.raw.RawClient sends 250 parallel simple POST request to /PostServletAsync and 
it does that very slow including sending the request headers, but (modern) servlet container process this asynchronously.

how about stripes framework?
----------------------------

The `net.sourceforge.stripes.controller.DispatcherServlet` process all POST requests blocking. The class
`AsyncDispatcherServlet` shows as a proof of concept how asynchronous processing could work. This works only within Tomcat, not Jetty.  

Remarks:
--------
- the attack works with very limited bandwidth, as it is not a large amount of data which kills the server, it is the slow connection from the client
