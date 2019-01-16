package com.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.utility.Constants;

@Component
public class ScheduledTaskService
{
	private int i = 0;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	//@Autowired
	//private ProcessPushMessage message;

    /**
     *
     */
    @Scheduled(initialDelay=0, fixedDelay=60000)
    public void executeAlarm()
    {
    	Resource resource = null;
    	ProcessPushMessage message = null;
    	Properties props = null;

    	try
        {
        	resource = new ClassPathResource(Constants.PROP_PATH);
        	props = PropertiesLoaderUtils.loadProperties(resource);
        	message = new ProcessPushMessage();
        	message.SetProps(props);
            //プッシュする処理を呼び出す
			message.pushBurnablesAlarm();

        }
        catch (URISyntaxException | SQLException | IOException | InstantiationException | IllegalAccessException | InterruptedException | ExecutionException e)
        {
        	System.out.println(e.getCause());
        }
    	finally
    	{
    		if (message != null)
    		{
    			message = null;
    		}
    		if (resource != null)
    		{
    			resource = null;
    		}
    		if (props != null)
    		{
    			props = null;
    		}
    	}

        System.out.println("実行回数: " + ++i + ", 実行時間: " + sdf.format(new Date()));
    }
}
