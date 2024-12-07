package com.amr.book.book;

import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {
    public static Specification<Book>withOwnerId(Integer ownerId){

        //what want to get we want the root.get("owner") and owner refer to which field i want
        //and i want to match this id with the ownerId
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("id"),ownerId));
    }
}
