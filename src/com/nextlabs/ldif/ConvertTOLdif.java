package com.nextlabs.ldif;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ConvertTOLdif {
	public static void main(String args[]) {
		
		String userObjectClass = "objectclass: user";
		String objectGUId = "objectGUID: {0}";
		String CHANGE_TYPE = "changetype: {0}";
		String CN="CN: {0}";
		String dc="dc=sap,dc=nextlabs,dc=com";
		int SAPUserId = 0;
		String sCurrentLine = null;
		String multiValueSeparator = "|";
		if (args.length > 0 && args[0] != null) {
			if (args.length > 1 && args[1] != null) {
				dc = args[1];
				System.out.println("DC detected and using \'"
						+ dc
						+ "\' for the creation of user CN");
			} else {
				System.out.println("DC not detected and using default \'"
						+ dc
						+ "\' for the creation of user CN");
			}
			if (args.length > 2 && args[2] != null) {
				multiValueSeparator = args[2];
				System.out.println("Multivalue Separator Detected and using \'"
						+ multiValueSeparator
						+ "\' in this process to identify MultiValue");
			} else {
				System.out
						.println("Multivalue Separator not Detected and using default \'"
								+ multiValueSeparator
								+ "\' in this process to identify MultiValue");
			}
			String dn = "dn: cn={0},"+dc;
			File file = new File(args[0]);
			File ldifFile = new File("SAPLdif.ldif");
			if (file.exists()) {
				try {
					FileReader fr = new FileReader(file);
					BufferedReader br = new BufferedReader(fr);
					FileWriter fw = new FileWriter(ldifFile);
					BufferedWriter bw = new BufferedWriter(fw);

					sCurrentLine = br.readLine();
					String[] str = sCurrentLine.substring(1,
							sCurrentLine.length() - 1).split("\",\"");
					ArrayList<String> keyList = new ArrayList<String>();
					int count = 0;
					int changeTypeCount = 1;
					boolean isChangeTypePresent = false;
					for (String token : str) {

						if (token.equalsIgnoreCase("SAPUserId")) {
							SAPUserId = count;
						}
						if (token.equalsIgnoreCase("changetype")) {
							changeTypeCount = count;
							isChangeTypePresent = true;
						}
						keyList.add(token.trim());
						count++;
					}
					while ((sCurrentLine = br.readLine()) != null) {
						try {
							count = 0;
							String id = "";
							String changeType = "add";
							str = sCurrentLine.substring(1,
									sCurrentLine.length() - 1).split("\",\"");
							ArrayList<String> valueList = new ArrayList<String>();

							for (String token : str) {

								if (count == SAPUserId) {
									id = token;
								}
								if (count == changeTypeCount) {
									changeType = token;
								}
								valueList.add(token);
								count++;
							}
							bw.write(MessageFormat.format(dn, id));
							bw.write("\n");
							if (isChangeTypePresent) {
								bw.write(MessageFormat.format(CHANGE_TYPE,
										changeType));
								bw.write("\n");
							}
							bw.write(userObjectClass);
							bw.write("\n");
							bw.write(MessageFormat.format(objectGUId, id));
							bw.write("\n");
							bw.write(MessageFormat.format(CN, id));
							bw.write("\n");
							for (int iteratorCount = 0; iteratorCount < valueList
									.size(); iteratorCount++) {
								String key = keyList.get(iteratorCount);
								String value = valueList.get(iteratorCount);
								if(key.equalsIgnoreCase("changetype")){
									continue;
								}
								StringBuilder sb = new StringBuilder();

								if (value.contains(multiValueSeparator)) {
									StringTokenizer multiValue = new StringTokenizer(
											value, multiValueSeparator);
									while (multiValue.hasMoreTokens()) {
										sb.append(key);
										sb.append(": ");
										sb.append(multiValue.nextToken());
										sb.append("\n");
									}
								} else {
									sb.append(key);
									sb.append(": ");
									sb.append(value);
									sb.append("\n");

								}
								bw.write(sb.toString());

							}

							bw.write("\n");

						} catch (Exception e) {

							System.out.println("Error in the following entry "
									+ sCurrentLine);
							System.out.println(e.getMessage());
						}
					}
					br.close();
					bw.close();

				} catch (Exception e) {
					System.out
							.println("Error while writing or reading a file . Exiting due to "
									+ e.getMessage());
					System.out.println("Error in the following entry "
							+ sCurrentLine);
					e.printStackTrace();
				}
			} else {
				System.out
						.println("File not found for  LDIF conversion . Exiting ...........");
			}
		} else {
			System.out
					.println("File not found for LDIF conversion . Exiting ...........");

		}
	}
}
