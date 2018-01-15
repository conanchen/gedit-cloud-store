[https://docs.docker.com/get-started/part2/#run-the-app]
1. Build the local app image 
    - $ docker build -t gedit-cloud-hello .
    - $ docker run -p 8088:8088 -p 8980:8980 gedit-cloud-hello
2. [https://docs.docker.com/get-started/part2/#share-your-image] 
3. Pull and run the image from the remote repository
	- $ docker run -p 8088:8088 -p 8980:8980 conanchen/gedit-cloud-hello:latest


- skip test
  - $ gradle build -x test
  - $  
- refresh dependencies
  - $ gradle build --refresh-dependencies
  - $ in itellij idea,remove caches like this:
   ```
   rm -rf /home/administrator/.gradle/caches/modules-2/files-2.1/com.github.conanchen.gedit-api-grpc/
   ```
   and then open gradle navigate view,click refresh button in the top left of view
- run jar file
  ```
  nohup java -Dspring.profiles.active=dev -jar /root/gedit-cloud-store-0.0.1-SNAPSHOT.jar>log/gedit_user/user.log &
  ```