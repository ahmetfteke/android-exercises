package com.tikle.mosms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

public class Util {
	public static final String operatorRootUrl="http://tools.tikle.com/SmsShortNumbers/AndroidSendSms_%s.txt";
	public static final String alertMsisdnUrl="http://tools.tikle.com/SmsShortNumbers/AlertMsisdns.txt";
	
	public static boolean isRunningCheckUrl = false;
	public static boolean sendMessage = false;

	private static String dirName = "MoSmsSender";

	public static String getStringFromUrl(String uri) throws ClientProtocolException, IOException {
		String result = "";

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(uri);
		httpGet.addHeader("Cache-Control", "no-cache");
		HttpResponse response;

		response = httpclient.execute(httpGet);

		HttpEntity ht = response.getEntity();

		BufferedHttpEntity buf = new BufferedHttpEntity(ht);

		InputStream is = buf.getContent();
		BufferedReader r = new BufferedReader(new InputStreamReader(is,
				"UTF-16"), 8192);

		StringBuilder total = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			total.append(line + "\n");
		}
		is.close();

		result = total.toString().trim().replace("\n", "");		

		return result;
	}

	public static void writeStringToFile(Context context, String opName, String data) {
		Writer writer;

		File root = context.getFilesDir();
		File outDir = new File(root.getAbsolutePath() + File.separator + dirName);
		if (!outDir.isDirectory()) {
			outDir.mkdir();
		}
		try {
			if (!outDir.isDirectory()) {
				throw new IOException(
						"Unable to create directory MoSmsSender. Maybe the SD card is mounted?");
			}
			File outputFile = new File(outDir, opName+".txt");
			writer = new BufferedWriter(new FileWriter(outputFile));
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			Logger.e("Util.writeStringToFile", e.getMessage());
			Logger.logToFile(context, e);
		}
	}

	public static String readStringFromFile(Context context, String opName) {
		BufferedReader br = null;
		String response = "";

		File root = context.getFilesDir();
		File outDir = new File(root.getAbsolutePath() + File.separator + dirName);
		if (!outDir.isDirectory()) {
			outDir.mkdir();
		}		

		try {
			File outputFile = new File(outDir, opName+".txt");
			StringBuffer output = new StringBuffer();				
			br = new BufferedReader(new FileReader(outputFile));
			String line = "";

			while ((line = br.readLine()) != null) {
				output.append(line +"\n");
			}

			response = output.toString();
			br.close();
		} catch (IOException e) {
			Logger.logToFile(context, e);
			e.printStackTrace();
			return "";
		}		
		return response;
	}

	public static String readNumbersFromAssets(Context context) {
		String text="";
		InputStream stream;
		try {
			stream = context.getAssets().open("noNetworkSmsNumbers.txt");
			int size = stream.available();
			byte[] buffer = new byte[size];
			stream.read(buffer);
			stream.close();
			text = new String(buffer);
			return text;
		} catch (IOException e) {
			Logger.logToFile(context, e);
			e.printStackTrace();
		}
		return text;
	}
}
