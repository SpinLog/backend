package com.example.spinlog.user.entity;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@DynamicInsert
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'NONE'")
    @Column(nullable = false)
    private Mbti mbti;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'NONE'")
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, unique = true)
    private String authenticationName; //oauth2 provider + "_" + oauth2 provider id

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Article> articles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    @Builder //Builder 에서 id 를 제외하기 위해, 클래스 레벨이 아닌 생성자 레벨에 @Builder 사용
    public User(String email, Mbti mbti, Gender gender, String authenticationName) {
        this.email = email;
        this.mbti = mbti;
        this.gender = gender;
        this.authenticationName = authenticationName;
    }

    public void change(String email) {
        this.email = email;
    }

    public void change(String mbti, String gender) {
        this.mbti = Mbti.valueOf(mbti);
        this.gender = Gender.valueOf(gender);
    }

    public void addArticle(Article article) {
        articles.add(article);
        article.setUser(this);
    }

    public void removeArticle(Article article) {
        articles.remove(article);
        article.setUser(null);
    }
    public Budget getCurrentMonthBudget() {
        LocalDate now = LocalDate.now();

        return budgets.stream()
                .filter(budget -> budget.isMonthOf(now))
                .findFirst()
                .orElseGet(() ->
                        addCurrentMonthBudget(0, now)
                );
    }

    public Budget addCurrentMonthBudget(Integer budgetValue, LocalDate now) {
        Budget budget = Budget.builder()
                .budget(budgetValue)
                .year(now.getYear())
                .month(now.getMonthValue())
                .user(this)
                .build();
        this.budgets.add(budget);

        return budget;
    }

    public Budget getBudgetOf(LocalDate localDate) {
        return budgets.stream()
                .filter(budget -> budget.isMonthOf(localDate))
                .findFirst()
                .orElse(null);
    }

    public User copyEntity() {
        User user = User.builder()
                .email(this.email)
                .mbti(this.mbti)
                .gender(this.gender)
                .authenticationName(this.authenticationName)
                .build();
        user.id = this.id;
        return user;
    }
}
