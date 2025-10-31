package org.f1.domain;

public record NSAD(Integer id,
                   int meetingEntityReference,
                   Integer actualPoints,
                   Double avgPoints,
                   Double avg4d1Points,
                   Double stdev) {
}
