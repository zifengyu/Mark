package markit;

import java.util.ArrayList;

public class Executor {

	public static void processStatus() throws Exception {

		MarkItDb db = MarkItDb.getInstance();
		ArrayList<String> idList = db.getUnprocessedStatus();

		try {
			for (int i = 0; i < idList.size(); ++i) {
				String id = idList.get(i);
				MailUtils.sendMail(id);
				System.out.println(id + " processed.");
				db.updateProcessedStatus(id);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Weibo4j.main(args);
			processStatus();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
