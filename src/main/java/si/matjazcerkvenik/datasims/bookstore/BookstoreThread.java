package si.matjazcerkvenik.datasims.bookstore;

public class BookstoreThread extends Thread {

    @Override
    public void run() {

        while (true) {

            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
