package com.amr.book.book;

import com.amr.book.common.PageResponse;
import com.amr.book.exception.OperationNotPermittedException;
import com.amr.book.file.FileStorageService;
import com.amr.book.history.TransactionHistoryRepository;
import com.amr.book.history.BookTransactionHistory;
import com.amr.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final BookMapper bookMapper;
    private final FileStorageService fileStorageService;
    public Integer save(BookRequest request, Authentication connectedUser){
        User user=((User) connectedUser.getPrincipal());
        Book book=bookMapper.toBook(request);
        book.setOwner(user);
         return bookRepository.save(book).getId();

    }
    public BookResponse findById(Integer bookId){
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(()->new EntityNotFoundException("No book found with the ID ::"+bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        //createdDate the same as in BaseEntity
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdDate").descending());
        /*as I want to display all the books except the connected user and I want to display the books
        which are only can be displayed

         */
        Page<Book> books=bookRepository.findAllDisplayedBooks(pageable,user.getId());
        List<BookResponse>bookResponses = books
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
          bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {

        User user =((User)connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdDate").descending());
        //JpaSpecificationExecutor in repository will give as the support for specification
        Page<Book> books=bookRepository.findAll(BookSpecification.withOwnerId(user.getId()),pageable);
        List<BookResponse>bookResponses = books
                .stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user =((User)connectedUser.getPrincipal());
        //as i always want to sort my element by the created date
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllBorrowedBooks(pageable, user.getId());
        List<BorrowedBookResponse>bookResponses=allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponses,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User user =((User)connectedUser.getPrincipal());
        //as i always want to sort my element by the created date
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = transactionHistoryRepository.findAllReturnedBooks(pageable, user.getId());
        List<BorrowedBookResponse>bookResponses=allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponses,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(()->new EntityNotFoundException("No book found with the ID:: "+ bookId));
        User user =((User)connectedUser.getPrincipal());
        if(!Objects.equals(book.getOwner().getId(),user.getId())){
            //throw an exception
            throw new OperationNotPermittedException("You cannot update others books shareable status");
        }
        //to inverse the value as
        /**
         * if the book is shareable and i want to stop sharing it we just need to change this true
         * to false
        **/
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()->new EntityNotFoundException("No book found with the ID:: "+ bookId));
        User user =((User)connectedUser.getPrincipal());
        if(!Objects.equals(book.getOwner().getId(),user.getId())){
            //throw an exception
            throw new OperationNotPermittedException("You cannot update others books archived status");
        }
        //to inverse the value as
        /**
         * if the book is shareable and i want to stop sharing it we just need to change this true
         * to false
         **/
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).
                orElseThrow(()->new EntityNotFoundException("No Book found with the ID:: "+bookId));

        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }
        User user = ((User)connectedUser.getPrincipal());
        if(!Objects.equals(book.getOwner().getId(),user.getId())){
            //throw an exception
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        final boolean isAlreadyBorrowed = transactionHistoryRepository.isAlreadyBorrowedByUser(bookId,user.getId());
        if(isAlreadyBorrowed){
            throw new OperationNotPermittedException("The requested book is already borrowed");
        }
        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).
                orElseThrow(()->new EntityNotFoundException("No Book found with the ID:: "+bookId));

        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }
        User user = ((User)connectedUser.getPrincipal());
        if(!Objects.equals(book.getOwner().getId(),user.getId())){
            //throw an exception
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }
        BookTransactionHistory bookTransactionHistory = transactionHistoryRepository.findByBookIdAndUserId(bookId,user.getId())
                .orElseThrow(()->new OperationNotPermittedException("You did not borrow this book"));

        bookTransactionHistory.setReturned(true);
        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnBorrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).
                orElseThrow(()->new EntityNotFoundException("No Book found with the ID:: "+bookId));

        if(book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }
        User user = ((User)connectedUser.getPrincipal());
        if(!Objects.equals(book.getOwner().getId(),user.getId())){
            //throw an exception
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }
        //to check if the connected user is the owner of the book
        BookTransactionHistory bookTransactionHistory = transactionHistoryRepository.findByBookIdAndOwnerId(bookId,user.getId())
                .orElseThrow(()->new OperationNotPermittedException("the book is not returned yet . You cannot approve this return it"));

        bookTransactionHistory.setReturnApproved(true);
        return transactionHistoryRepository.save(bookTransactionHistory).getId();

    }

    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        // i will create file storage service to upload the file
        //things before the storage service
        Book book = bookRepository.findById(bookId).
                orElseThrow(()->new EntityNotFoundException("No Book found with the ID:: "+bookId));

        User user = ((User)connectedUser.getPrincipal());
        //user.getId() as foreach user i want to create a folder where i will upload all the files
        // that are belong to this specific user

       var bookCover = fileStorageService.saveFile(file,user.getId());
       book.setBookCover(bookCover);
       bookRepository.save(book);

    }
}
