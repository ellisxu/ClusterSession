# ClusterSession
This is a clustered session solution for Java clustering web-applications. In general, when we're providing a formal Java web-application for users, it's wise and common to deploy the application into a cluster involving a couple of, even hundreds of, servers with separate memory spaces. In this situation, we have to make sessions accessible for each separate server. 


To solve this problem, you could rely on clusterd-session extensions provided by those popular servlet container like Tomcat and Resin. Sometimes, if you want to make your application more tranferable without dependence on a specific servlet container, you could use something like Spring Session Management. However, if you dislike integrating Spring Framework into your application, ClusterSession could be a good choice for you! Of course, ClusterSession is compatible when you've integrated Spring Framework.
  


