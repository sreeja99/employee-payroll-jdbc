package com.capgemini.employeepayrolljdbc;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class Java8WatchServiceExample {
	private static final Kind<?> ENTRY_CREATE = null;
	private static final Kind<?> ENTRY_DELETE = null;
	private static final Kind<?> ENTRY_MODIFY = null;
	private final WatchService watcher; 
	private final Map<WatchKey,Path> dirWatchers;
	
	//Creates a watch service and registers the given directory
	public Java8WatchServiceExample(Path dir) throws IOException{
		this.watcher = FileSystems.getDefault().newWatchService();
		this.dirWatchers = new HashMap<WatchKey,Path>();
		scanAndRegisterDirectories(dir);
	}
	
	//Register given directory with watch service
	private void registerDirWatchers(Path dir) throws IOException{
		WatchKey key = dir.register(watcher,ENTRY_CREATE,ENTRY_DELETE,ENTRY_MODIFY);
		dirWatchers.put(key, dir);
	}
	
	//register directory and sub directories
	private void scanAndRegisterDirectories(final Path start) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>(){
		@Override
		public FileVisitResult preVisitDirectory(Path dir,BasicFileAttributes attrs) throws IOException{
			registerDirWatchers(dir);
			return FileVisitResult.CONTINUE;
		}
		});
	}
	@SuppressWarnings({"rawtypes","unchecked"})
	public void processEvents() {
		while(true) {
			WatchKey key;
			try {
				key = watcher.take();
			}catch(InterruptedException x) {
				return;
			}
			Path dir = dirWatchers.get(key);
			if(dir==null)
				continue;
			for(WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();
				Path name = ((WatchEvent<Path>)event).context();
				Path child = dir.resolve(name);
				System.out.format("%s : %s\n", event.kind().name(),child);
				
				if(kind == ENTRY_CREATE) {
					try {
						if(Files.isDirectory(child))
							scanAndRegisterDirectories(child);
					}catch (IOException x) {}
				}else if(kind.equals(ENTRY_DELETE)) {
					if(Files.isDirectory(child))
						dirWatchers.remove(key);
				}
				
			}
			boolean valid = key.reset();
			if(!valid) {
				dirWatchers.remove(key);
				if(dirWatchers.isEmpty())
					break;
			}
		}
	}
}
