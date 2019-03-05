package com.binaryfountain.devtools.instrument.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

/**
 * Transforms DBMetedata class to connect to local mysql and load metadata.
 * 
 * @author Kiliyani
 *
 */
public class DBMetedataTransformer implements ClassFileTransformer {

	final static String TARGET_CLASS = "com.binaryfountain.compass.sql.model.DBMetadata";

	private String url;
	private String username;
	private String password;

	public DBMetedataTransformer(String url, String username, String password) {
		super();
		this.url = url == null ? "jdbc:mysql://localhost:3306/database" : url;
		this.username = username == null ? "root" : username;
		this.password = password == null ? "root" : password;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;

		String targetClassName = TARGET_CLASS.replaceAll("\\.", "/");
		if (!className.equals(targetClassName)) {
			return byteCode;
		}

		if (className.equals(targetClassName)) {
			System.out.println("[Agent] Transforming class DBMetadata");
			try {
				ClassPool cp = ClassPool.getDefault();
				cp.insertClassPath(new ByteArrayClassPath(TARGET_CLASS, byteCode));

				Class<?> class1 = Class.forName("com.binaryfountain.compass.sql.util.SQLDatasource", false, loader);
				cp.insertClassPath(new ClassClassPath(class1));

				CtClass cc = cp.get(TARGET_CLASS);
				CtConstructor constructor = cc.getConstructor("(Ljava/lang/String;)V");
				constructor.setBody(getBody());
				byteCode = cc.toBytecode();
				cc.detach();
				System.out.println("[Agent] DBMetadata class transformed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return byteCode;
	}

	private String getBody() {

		return "{ this.m_resourceId = $1;\r\n" + "	    java.sql.Connection conn = null;\r\n" + "	    try {\r\n"
				+ "	    	com.binaryfountain.compass.sql.util.SQLDatasource datasource = new com.binaryfountain.compass.sql.util.SQLDatasource();\r\n"
				+ "	    	datasource.setDriverClassName(\"com.mysql.jdbc.Driver\");\r\n"
				+ "	    	datasource.setUrl(\"" + this.url + "\".replaceAll(\"database$\", $1.equals(\"client\") ? \"compass\" : $1) + \"?characterEncoding=UTF-8\");\r\n"
				+ "	    	datasource.setUsername(\"" + this.username + "\");\r\n"
				+ "            datasource.setPassword(\"" + this.password + "\");\r\n"
				+ "		    conn = datasource.getConnection();\r\n"
				+ "			java.sql.DatabaseMetaData metadata = conn.getMetaData();\r\n"
				+ "			this.m_tables = this.readTables(metadata);\r\n"
				+ "			this.m_resourceName = metadata.getDatabaseProductName();\r\n" + "	    }\r\n"
				+ "	    finally {\r\n" + "	    	if (conn != null) {\r\n" + "	    		conn.close();\r\n"
				+ "	    	}\r\n"
				+ "	    } }";

	}

}
