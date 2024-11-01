package si.matjazcerkvenik.datasims.bookstore;

import java.util.ArrayList;
import java.util.List;

public class BookstoreMain {

    private static List<Book> books = new ArrayList<>();

    public static void main(String[] args) {

        fillBooks();

        BookstoreThread bt = new BookstoreThread();
        bt.start();

    }

    private static void fillBooks() {
        books.add(new Book("J. K. Rowling", "Harry Potter", 29.99));
    }

}
