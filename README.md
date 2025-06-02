# ArcherBooks

**Archer Books** is a proposed book-borrowing application for De La Salle University’s The Libraries. It allowsthe Lasallian community, including students and the academic staff, to search for books across millions of works. Google Books API was used for the database of books instead of the actual books from The Libraries. Search queries can be filtered by book title, author name, subject, and publisher name and can be sorted by relevance, number of editions, and publication date.

After a book has been selected, the student/academic staff user can reserve the book for a period of time, maximum of seven days, as long as the book is not reserved for the selected consecutive dates. The pickup and return dates of the book are the start and end dates indicated in the request in the mobile application. On the day of pickup and return, the library administrator (i.e., a separate role) will change the status of the transaction on their end in the mobile application, and only then will it be reflected on the side of the user.

### Dependencies
1. Firestore Database
    - Store user accounts (student, academic staff, library administrator)
    - Store borrowed books and their borrowing status/information
    - Store list of favorites mapped to user accounts
    - Store borrowing transactions mapped to user accounts

2. Firebase Auth
    - Allow user signup and login
    - Retrieve Google account's personal information
  
3. Google Books API
