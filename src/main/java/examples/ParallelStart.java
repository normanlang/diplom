package examples;

import geomason.Room;

public class ParallelStart {

	public static void main(String[] args){
		Thread t1 = new Thread() {
			public void run(){
				new Room(System.currentTimeMillis());
				String[] args = new String[0];
				Room.doLoop(Room.class, args);
		        System.exit(0);
			}
		};
		Thread t2 = new Thread() {
			public void run(){
				new Room(System.currentTimeMillis());
				String[] args = new String[0];
				Room.doLoop(Room.class, args);
		        System.exit(0);
			}
		};
		Thread t3 = new Thread() {
			public void run(){
				new Room(System.currentTimeMillis());
				String[] args = new String[0];
				Room.doLoop(Room.class, args);
		        System.exit(0);
			}
		};
		t1.start();
		t2.start();
		t3.start();
	}
}
