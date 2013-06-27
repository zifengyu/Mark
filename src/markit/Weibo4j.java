package markit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import markit.MarkItDb;

import weibo4j.*;
import weibo4j.examples.oauth2.Log;
//import weibo4j.http.*;
import weibo4j.model.Paging;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.WeiboException;
import weibo4j.util.*;


public class Weibo4j {

	public static void main(String args[]) throws Exception {
		MarkItDb db = MarkItDb.getInstance();

		Oauth oauth = new Oauth();
	
		String url = oauth.authorize("code", "");
		
		WebDriver driver = new FirefoxDriver();
		driver.get(url);
		
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.name("userId")));
		
		WebElement element = driver.findElement(By.name("userId"));
		element.sendKeys("biaoji@hotmail.com");
		
		element = driver.findElement(By.name("passwd"));
		element.sendKeys("54746181");
		
		element.submit();
		
		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {            	
                return d.getCurrentUrl().contains("code=");
            }
        });
		
		url = driver.getCurrentUrl();
		String code = url.substring(url.indexOf("code=") + 5);
		
		driver.close();
		
		String access_token = null;
		try{
			access_token = oauth.getAccessTokenByCode(code).getAccessToken();
			System.out.println(access_token);
		} catch (WeiboException e) {
			if(401 == e.getStatusCode()){
				Log.logInfo("Unable to get the access token.");
			}else{
				e.printStackTrace();
			}
		}
		Timeline tm = new Timeline();
		tm.client.setToken(access_token);
		try {
			StatusWapper status = tm.getMentions(new Paging(1, 200, db.getMaxStatusId()), 1, 0, 0);
			for(Status s : status.getStatuses()){
				if (s.getText().trim().startsWith("@标记为")) {
					Status rs = s.getRetweetedStatus();
					String rid = "0";
					if (rs != null) {
						System.out.println(rs);
						rid = rs.getId();
						try {
							db.insertStatus(rs.getId(), "0", rs.getText(), rs.getOriginalPic(), rs.getUser().getId(), 1);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					try {
						db.insertStatus(s.getId(), rid, s.getText(), s.getOriginalPic(), s.getUser().getId(), 0);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					System.out.println(s);
				}
			}		
		} catch (WeiboException e) {
			e.printStackTrace();
		}
	}

}
