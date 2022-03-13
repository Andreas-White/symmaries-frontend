package logic.general;

import soot.RefLikeType;
import soot.RefType;

public class Constants {
	public static final String NIL = "Nil";
	public static final String dummyClass = "dummyClass";
	public static final String lowSinkMethodName = "lowSink";
	public static final String highSourceMethodName = "highSource";
	public static final String highSinkMethodName = "highSink";
	public static final String lowSourceMethodName = "lowSource";
	public static final String This = "this";
	public static final RefLikeType throwableType = RefType.v("java.lang.Throwable");
	public static final Object OBJECT = "java.lang.Object";
	public static final long serialVersionUID = 1L;
	public static final String statsFilePrefix = "results";
	public static final String meth_statsFileExtension = ".meth_stats";
	public static final String secsumFileExtension = ".secsums";
	public static final String secstubsFileExtension = ".secstubs";
	public static final String GroundTruthFile = "ground-truth.txt";
	public static final String methExtension = "meth";
	public static final String srcsExtension = "srcs";
	public static final String symmariesPath = "syrs"; // System.getProperty("user.home") + "/.opam/ocaml-base-compiler.4.07.1/bin/scgs.opt";
	public static final String secsumPath = "/Volumes/Academics/Dropbox/JisymCOmpiler/third-party.secstubs";
;
	

}
