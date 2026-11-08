package gal.usc.etse.es.tapeacompostela.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;


// Una excepción que lleva su propio ProblemDetail: Spring la responde sin ningún @ExceptionHandler.
// 422 Unprocessable Entity: la petición es correcta, pero las reglas del negocio no la permiten
public class NotEnoughPointsException extends ErrorResponseException {

    public NotEnoughPointsException(int cost, int points) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, asProblemDetail(cost, points), null);
    }

    private static ProblemDetail asProblemDetail(int cost, int points) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY,
                "The reward costs " + cost + " points and you have " + points);
        problem.setTitle("Not enough points");
        problem.setProperty("cost", cost);
        problem.setProperty("points", points);
        return problem;
    }
}