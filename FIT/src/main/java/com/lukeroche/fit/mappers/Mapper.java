package com.lukeroche.fit.mappers;

public interface Mapper<A,B> {

    B mapToResponse(A a);

    A mapFromRequest(B b);
}
