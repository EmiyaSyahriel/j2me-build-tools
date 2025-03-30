/**
 * This install script requires at least Java 11
 */
import java.io.*;
import java.lang.*;
import java.util.*;

public class MavenLocalInstaller
{
	public static void main(String[] args)
	{
		ArrayList<Jar> jars = new ArrayList<Jar>();
		File fList = new File("install-list.txt");
		try(BufferedReader br= new BufferedReader(new FileReader(fList)))
		{
			for(String line; (line = br.readLine()) != null; )
			{
				if(line.trim().length() == 0) continue;
				if(line.trim().startsWith("#")) continue;
				
				String[] jLine = line.trim().split("=");
				jars.add(new Jar(jLine[0], jLine[1]));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		Runtime runtime = Runtime.getRuntime();
		for(Jar jar : jars)
		{
			jar.exec(runtime);
		}

		System.exit(0);
	}
}

public class Jar
{
	private String _fileName;
	private String _groupId;
	private String _version;
	private String _artifactId;
		
	public Jar(String fileName, String mvnName)
	{
		String[] parts = mvnName.split(":");
		_groupId = parts[0];
		_artifactId = parts[1];
		_version = parts[2];
		_fileName = fileName;
	}

	private static String getOnPath(String executable)
	{
		String path = System.getenv("PATH");
		String[] dirs = path.split(File.pathSeparator);
		for( String dir: dirs)
		{
			File f = new File(dir, executable);
			if(f.exists()) return f.getAbsolutePath();
		}
		
		return null;
	}

	public void exec(Runtime runtime)
	{
		System.out.println(String.format("Installing : %s as %s:%s:%s", _fileName, _groupId, _artifactId, _version));
		String[] args = new String[]{
		};

		try 
		{	
			Boolean isWindows = System.getProperty("os.name", "Linux").startsWith("Windows");
			ProcessBuilder pb = new ProcessBuilder(
				getOnPath(isWindows ? "mvn.cmd" : "mvn"),
				"install:install-file", 
				String.format("-Dfile=\"%s\"", _fileName),
				String.format("-DgroupId=\"%s\"", _groupId),
				String.format("-DartifactId=\"%s\"", _artifactId),
				String.format("-Dversion=\"%s\"", _version),
				"-Dpackaging=jar"
			);
			pb.inheritIO();
			
			Process p = pb.start();

			int exitCode = p.waitFor();
			String eCode = exitCode == 0 ? "Ok!" : String.format("ERROR : %d", exitCode);
			System.out.println(eCode);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}