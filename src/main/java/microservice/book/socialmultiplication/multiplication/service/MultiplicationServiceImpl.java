package microservice.book.socialmultiplication.multiplication.service;

import lombok.RequiredArgsConstructor;
import microservice.book.socialmultiplication.multiplication.domain.Multiplication;
import microservice.book.socialmultiplication.multiplication.domain.MultiplicationResultAttempt;
import microservice.book.socialmultiplication.multiplication.domain.User;
import microservice.book.socialmultiplication.multiplication.event.EventDispatcher;
import microservice.book.socialmultiplication.multiplication.event.MultiplicationSolvedEvent;
import microservice.book.socialmultiplication.multiplication.repository.MultiplicationResultAttemptRepository;
import microservice.book.socialmultiplication.multiplication.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class MultiplicationServiceImpl implements MultiplicationService {

    private final RandomGeneratorService randomGeneratorService;
    private final MultiplicationResultAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final EventDispatcher eventDispatcher;


    @Override
    public Multiplication createRandomMultiplication() {
        int factorA = randomGeneratorService.generateRandomFactor();
        int factorB = randomGeneratorService.generateRandomFactor();
        return new Multiplication(factorA, factorB);
    }

    @Transactional
    @Override
    public boolean checkAttempt(final MultiplicationResultAttempt attempt) {
        // Check if the user already exists for that alias
        Optional<User> user = userRepository.findByAlias(attempt.getUser().getAlias());

        // Avoids 'hack' attempts
        Assert.isTrue(!attempt.isCorrect(), "You can't send an attempt marked as correct!!");

        // Check if the attempt is correct
        boolean isCorrect = attempt.getResultAttempt() ==
                attempt.getMultiplication().getFactorA() *
                        attempt.getMultiplication().getFactorB();
        MultiplicationResultAttempt checkedAttempt = MultiplicationResultAttempt.builder()
                .user(user.orElse(attempt.getUser()))
                .multiplication(attempt.getMultiplication())
                .resultAttempt(attempt.getResultAttempt())
                .correct(isCorrect)
                .build();

        // Stores the attempt
        attemptRepository.save(checkedAttempt);

        // Communicates the result via Event
        eventDispatcher.send(
                new MultiplicationSolvedEvent(checkedAttempt.getId(),
                        checkedAttempt.getUser().getId(),
                        checkedAttempt.isCorrect())
        );

        return isCorrect;
    }

    @Override
    public List<MultiplicationResultAttempt> getStatsForUser(final String userAlias) {
        return attemptRepository.findTop5ByUserAliasOrderByIdDesc(userAlias);
    }

    @Override
    public MultiplicationResultAttempt getResultById(final Long resultId) {
        return attemptRepository.findById(resultId).orElse(new MultiplicationResultAttempt());
    }


}
