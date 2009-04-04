package captainfanatic.bites;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;

/** Listen for received sms and interpret raw pdus into messages to check for recipes
 * 
 * @author ben
 *
 */
public class SmsReceiver extends BroadcastReceiver {
	
	public static final String KEY_MSG_TEXT = "messageText";
	public static final String KEY_RECIPE = "recipeName";
	public static final String KEY_ING_ARRAY = "ingredientArray";
	public static final String KEY_METH_ARRAY = "methodArray";
	
	private static int ID = 0;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String message = "asdf";
		
		if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			return;
		}
		
		Bundle bdl = intent.getExtras();
		
		Object pdus[] = (Object[])bdl.get("pdus");
		for (int n=0; n<pdus.length; n++) {
			byte byteData[] = (byte[])pdus[n];
			message = SmsMessage.createFromPdu(byteData).getDisplayMessageBody();
		}
		
		if (message.contains("***Bites Recipe***")) {
	
			
			/*NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			//increment the notificaton id
			ID += 1;
			PendingIntent pendIntent = PendingIntent.getActivity(
															context, 
															0, 
															new Intent(), 
															0);
			Notification notification = new Notification(R.drawable.icon, "Recipe Received", );
			nm.notify(ID, notification);*/
			
			Intent broadcast = new Intent("com.captainfanatic.bites.RECEIVED_RECIPE");
			broadcast.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			broadcast.putExtra(KEY_MSG_TEXT, message);
			//Recipe name
			broadcast.putExtra(KEY_RECIPE, "");
			
			//ingredients array
			broadcast.putExtra(KEY_ING_ARRAY, "");
			
			//Methods array
			broadcast.putExtra(KEY_METH_ARRAY, "");
			
			try {
				context.startActivity(broadcast);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}				
	}
}
