//package com.studit.backend.domain.todoList;
//
//import com.studit.backend.domain.todoList.entity.Enum.TodoEndType;
//import jakarta.persistence.PrePersist;
//
//import javax.swing.text.html.HTML;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import org.hibernate.annotations.ColumnDefault;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity(name = "TODOS")
//@Getter
//public class Todo {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "todo_id")
//    private Long todoId;
//
//    @Column(name = "todo_name", nullable = false)
//    private String todoName;
//
//    // 총 사용시간 컬럼 (초 단위로 저장)
//    @Column(name = "total_study_time", nullable = false)
//    @ColumnDefault("0")
//    private Long totalStudyTime = 0L; // 기본값 0초로 설정
//
//
//    // 생성일시 컬럼
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "end_yn", nullable = false, length = 1)
//    @ColumnDefault("'N'")
//    private TodoEndType endYN;
//
//
//    @PrePersist
//    public void setDefaultValues() {
//        if (endYN == null) {
//            HTML.Attribute TodoEndType;
//            endYN = com.studit.backend.domain.todoList.entity.Enum.TodoEndType.N; // 기본값 N로 설정
//        }
//
//        if (totalStudyTime == null) {
//            totalStudyTime = 0L; // 기본값 0초로 설정
//        }
//    }
//
//}