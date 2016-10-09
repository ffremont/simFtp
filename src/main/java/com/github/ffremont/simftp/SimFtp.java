/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.simftp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.config.spring.UserManagerBeanDefinitionParser;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

/**
 *
 * @author florent
 */
public class SimFtp {
    
    private static final String DEFAULT_AUTH = "admin:admin";
    private static final int DEFAULT_PORT = 2121;

    public static void main(String[] args) throws FtpException {
        if (args.length > 0) {
            if ("help".equals(args[0])) {
                System.out.println(new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("helper.txt")))
                         .lines().collect(Collectors.joining("\n")));
                return;
            }
        }

        String auth = System.getProperty("auth") != null ? System.getProperty("auth") : DEFAULT_AUTH;
        String dir = System.getProperty("dir") != null ? System.getProperty("dir") : System.getProperty("user.home");
        int port = System.getProperty("port") != null ? Integer.valueOf(System.getProperty("port")) : DEFAULT_PORT;
        String[] splitAuth = auth.split(":");
        if(splitAuth.length != 2){
            System.err.println("le format du paramètre 'auth' n'est pas valide, nomUtilisateur:motDePasse");
            return;
        }
        
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();
        BaseUser user = new BaseUser();
        
        user.setName(splitAuth[0]);
        user.setPassword(splitAuth[1]);
        user.setHomeDirectory(dir);
        userManager.save(user);

        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(port);

        FtpServerFactory factory = new FtpServerFactory();
        factory.setUserManager(userManager);
        
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        try {
            userManager.save(user);//Save the user to the user list on the filesystem
        } catch (FtpException e1) {
            e1.printStackTrace(System.err);
        }        
        factory.addListener("default", listenerFactory.createListener());

        FtpServer server = factory.createServer();
        server.start();
    }

}
