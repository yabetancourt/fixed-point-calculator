package com.yabetancourt.application.views.helloworld;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import groovy.util.Eval;

import java.util.Arrays;
import java.util.function.Function;

@PageTitle("Punto Fijo")
@Route(value = "/")
@RouteAlias(value = "")
public class FixedPointMethodView extends VerticalLayout {

    private static int k;

    public FixedPointMethodView() {
        setSizeFull();

        H1 title = new H1("Método de Punto Fijo");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("400px", 1));
        formLayout.setWidth(400, Unit.PIXELS);
        TextField function = new TextField("G(x)");
        function.setRequired(true);
        function.setClearButtonVisible(true);
        function.setPlaceholder("(3*x-1)/(x*x)");
        function.setHelperText("Introduzca una función tal que F(x) = x - G(x).");
        NumberField initialValue = new NumberField("Valor Inicial");
        initialValue.setRequiredIndicatorVisible(true);
        initialValue.setPlaceholder("0");
        initialValue.setClearButtonVisible(true);
        NumberField tolerance = new NumberField("Tolerancia");
        tolerance.setPlaceholder("0.001");
        tolerance.setRequiredIndicatorVisible(true);
        tolerance.setClearButtonVisible(true);
        IntegerField maxIter = new IntegerField("Número máximo de iteraciones");
        maxIter.setRequiredIndicatorVisible(true);
        maxIter.setPlaceholder("50");
        maxIter.setClearButtonVisible(true);
        formLayout.add(function, initialValue, tolerance, maxIter);

        Button searchRoot = new Button("Buscar");
        searchRoot.addClickShortcut(Key.ENTER);
        searchRoot.addClickListener(click -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setConfirmText("Aceptar");
            dialog.setCancelable(false);
            dialog.addConfirmListener(c -> dialog.close());

            if (isAnyEmpty(function, initialValue, tolerance, maxIter)) {
                function.setInvalid(function.isEmpty());
                initialValue.setInvalid(initialValue.isEmpty());
                tolerance.setInvalid(tolerance.isEmpty());
                maxIter.setInvalid(maxIter.isEmpty());
            } else {
                dialog.open();
                try {
                    double root = findRoot(initialValue.getValue(), tolerance.getValue(), maxIter.getValue(), function.getValue());
                    Span rootText = new Span("La raíz es: " + root);
                    Span iterText = new Span("Cantidad de iteraciones: " + k);
                    VerticalLayout text = new VerticalLayout(rootText, iterText);
                    dialog.setText(text);
                } catch (NotConvergentException e) {
                    dialog.setText("El método no ha convergido a la solución requerida. Pruebe a aumentar el número máximo " +
                            "de iteraciones o escoga otra función G(x).");
                }
            }

        });
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addAndExpand(searchRoot);
        formLayout.add(layout);

        setMargin(true);
        setHorizontalComponentAlignment(Alignment.CENTER, title, formLayout);

        add(title, formLayout);
    }

    public static Function<Double, Double> fromStringToFunction(String expression) {
        return x -> (double) Eval.x(x, expression);
    }

    private static double findRoot(double initialValue, double tolerance, int maxSteps, String expression) throws NotConvergentException {
        k = 0;
        double x1, x0 = initialValue;
        x1 = x0;

        do {
            k++;
            x0 = x1;
            x1 = fromStringToFunction(expression).apply(x1);
        } while (k <= maxSteps && Math.abs(x1 - x0) > tolerance);

        if (k > maxSteps && Math.abs(x1 - x0) > tolerance) {
            throw new NotConvergentException();
        }

        return x1;

    }

    private static boolean isAnyEmpty(AbstractField... components) {
        return Arrays.stream(components).anyMatch(AbstractField::isEmpty);
    }

}
