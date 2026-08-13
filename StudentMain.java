package rs.ac.bg.etf.sab;

import rs.ac.bg.etf.sab.operations.*;
import rs.ac.bg.etf.sab.tests.TestHandler;
import rs.ac.bg.etf.sab.tests.TestRunner;
import student.*;

public class StudentMain {
    public static void main()  {

        GeneralOperations generalOperations = new rd210107_GeneralOperations();
        GenresOperations genresOperations = new rd210107_GenresOperations();
        MoviesOperations moviesOperations = new rd210107_MoviesOperations();
        RatingsOperations ratingsOperation = new rd210107_RatingsOperations();
        TagsOperations tagsOperations = new rd210107_TagsOperations();
        UsersOperations usersOperations = new rd210107_UsersOperations();
        WatchlistsOperations watchlistsOperations = new rd210107_WatchlistsOperations();

        TestHandler.createInstance(
                genresOperations,
                moviesOperations,
                ratingsOperation,
                tagsOperations,
                usersOperations,
                watchlistsOperations,
                generalOperations);
        TestRunner.runTests();
    }
}
