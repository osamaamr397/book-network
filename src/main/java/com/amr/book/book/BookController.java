package com.amr.book.book;

import com.amr.book.common.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.Introspection;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "books")
@Tag(name ="Book")
public class BookController {
    private final BookService service;
    @PostMapping
    public ResponseEntity<Integer>saveBook(
            @Valid @RequestBody BookRequest request,
            Authentication connectedUser
    )
    {
        return ResponseEntity.ok(service.save(request,connectedUser));
    }
    @GetMapping("{book-id}")
    public ResponseEntity<BookResponse>findBookById(
            @PathVariable("book-id")Integer bookId
    ){
        return ResponseEntity.ok(service.findById(bookId));
    }
    @GetMapping()
    //as if i have 3000 books there will be head on the traffic to load it so i used the pagination
    public ResponseEntity<PageResponse<BookResponse>>findAllBooks(
            @RequestParam(name = "page",defaultValue = "0",required = false)int page,
            @RequestParam(name = "size",defaultValue = "0",required = false)int size,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(service.findAllBooks(page,size,connectedUser));
    }
    @GetMapping("/owner")
    public ResponseEntity<PageResponse<BookResponse>>findAllBooksByOwner(
            @RequestParam(name = "page",defaultValue = "0",required = false)int page,
            @RequestParam(name = "size",defaultValue = "0",required = false)int size,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(service.findAllBooksByOwner(page,size,connectedUser));
    }

    //we want to fetch all borrowed books by the connected user so we put the parameter which belong to the user

    @GetMapping("/borrowed")
    public ResponseEntity<PageResponse<BorrowedBookResponse>>findAllBorrowedBooks(
            @RequestParam(name = "page",defaultValue = "0",required = false)int page,
            @RequestParam(name = "size",defaultValue = "0",required = false)int size,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(service.findAllBorrowedBooks(page,size,connectedUser));
    }

    @GetMapping("/returned")
    public ResponseEntity<PageResponse<BorrowedBookResponse>>findAllReturnedBooks(
            @RequestParam(name = "page",defaultValue = "0",required = false)int page,
            @RequestParam(name = "size",defaultValue = "0",required = false)int size,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(service.findAllReturnedBooks(page,size,connectedUser));
    }
    @PatchMapping("/shareable/{book-id}")
    public ResponseEntity<Integer>updateShareableStatus(
            @PathVariable("book-id")Integer bookId,
            Authentication connectedUser
            ){
        return ResponseEntity.ok(service.updateShareableStatus(bookId,connectedUser));
    }

    @PatchMapping("/archived/{book-id}")
    public ResponseEntity<Integer>updateArchivedStatus(
            @PathVariable("book-id")Integer bookId,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(service.updateArchivedStatus(bookId,connectedUser));
    }

    @PostMapping("/borrow/{book-id}")
    public ResponseEntity<Integer>borrowBook(
            @PathVariable("book-id")Integer bookId,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(service.borrowBook(bookId,connectedUser));
    }

    @PatchMapping("/borrow/return/{book-id}")
    public ResponseEntity<Integer>returnBorrowBook(@PathVariable("book-id")Integer bookId,
                                                   Authentication connectedUser){
         return ResponseEntity.ok(service.returnBorrowedBook(bookId,connectedUser));
    }
    @PatchMapping("/borrow/return/approve/{book-id}")
    public ResponseEntity<Integer>approveReturnBorrowBook(@PathVariable("book-id")Integer bookId,
                                                   Authentication connectedUser){
        return ResponseEntity.ok(service.approveReturnBorrowBook(bookId,connectedUser));
    }

    @PostMapping(value = "/cover/{book-id}",consumes = "multipart/form-data")
    public ResponseEntity<?>uploadBookCoverPicture(
            @PathVariable("book-id")Integer bookId,
            @Parameter()
            @RequestPart("file")MultipartFile file,
            Authentication connectedUser

            ){
        service.uploadBookCoverPicture(file,connectedUser,bookId);
        return ResponseEntity.accepted().build();
    }

}
