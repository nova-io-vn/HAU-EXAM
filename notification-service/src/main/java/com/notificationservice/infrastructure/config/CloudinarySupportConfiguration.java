package com.notificationservice.infrastructure.config;
import com.cloudinary.Cloudinary; import com.cloudinary.utils.ObjectUtils; import org.springframework.boot.context.properties.*; import org.springframework.context.annotation.*;
@Configuration @EnableConfigurationProperties(CloudinarySupportConfiguration.Properties.class)
public class CloudinarySupportConfiguration { @Bean Cloudinary cloudinary(Properties p){return new Cloudinary(ObjectUtils.asMap("cloud_name",p.cloudName(),"api_key",p.apiKey(),"api_secret",p.apiSecret()));}
 @ConfigurationProperties(prefix="cloudinary") public record Properties(String cloudName,String apiKey,String apiSecret,@org.springframework.boot.context.properties.bind.DefaultValue("5242880") long imageMaxSizeBytes){public boolean configured(){return cloudName!=null&&!cloudName.isBlank()&&apiKey!=null&&!apiKey.isBlank()&&apiSecret!=null&&!apiSecret.isBlank();}}
}
