package com.binaryfountain.devtools.instrument.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Transforms DBTable class to return capital table names.
 * 
 * @author Kiliyani
 *
 */
public class DBTableTransformer implements ClassFileTransformer {
	
	final static String TARGET_CLASS = "com.binaryfountain.compass.sql.model.DBTable";

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;

		String targetClassName = TARGET_CLASS.replaceAll("\\.", "/");
		if (!className.equals(targetClassName)) {
			return byteCode;
		}
		
		if (className.equals(targetClassName)) {
			System.out.println("[Agent] Transforming class DBTable");
			try {
				ClassPool cp = ClassPool.getDefault();
				cp.insertClassPath(new ByteArrayClassPath(TARGET_CLASS, byteCode));
				
				CtClass cc = cp.get(TARGET_CLASS);
				CtMethod m = cc.getDeclaredMethod("getName");
				m.setBody("{return this.m_name.toUpperCase();}");
				byteCode = cc.toBytecode();
				cc.detach();
				System.out.println("[Agent] DBTable class transformed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return byteCode;
	}

}
