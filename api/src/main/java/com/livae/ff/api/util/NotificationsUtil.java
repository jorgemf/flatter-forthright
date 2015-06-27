package com.livae.ff.api.util;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.livae.ff.api.Settings;
import com.livae.ff.api.model.PhoneUser;
import com.livae.ff.common.Constants;

import java.io.IOException;

import static com.livae.ff.api.OfyService.ofy;

public class NotificationsUtil {

	public static void sendPushNotification(PhoneUser user, String message,
											Constants.PushNotificationType type)
	  throws IOException, InternalServerErrorException {
		// find devices
		Sender sender = new Sender(Settings.Google.API_KEY);
		Message msg = new Message.Builder().addData("m", message).addData("t", type.name())
										   .timeToLive(Settings.NOTIFICATIONS_TIME_TO_LIFE).build();
		String deviceId = user.getDeviceId();
		Result result = sender.send(msg, deviceId, Settings.GCM_NOTIFICATION_RETRIES);
		if (result.getMessageId() != null) {
			String canonicalRegId = result.getCanonicalRegistrationId();
			if (canonicalRegId != null) {
				// if the device id changed, we have to update the user device
				user.setDeviceId(canonicalRegId);
				ofy().save().entity(user);
			}
		} else {
			// else there was an error somewhere
			throw new InternalServerErrorException("Error while sending notification, codename: " +
												   result.getErrorCodeName());
		}
	}

}