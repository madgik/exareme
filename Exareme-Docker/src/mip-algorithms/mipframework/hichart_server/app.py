from flask import Flask, render_template

from mipframework.hichart_server.algorun import get_algorithm_result

from PCA import PCA
from PEARSON_CORRELATION import Pearson
from LOGISTIC_REGRESSION import LogisticRegression
from CALIBRATION_BELT import CalibrationBelt
from KAPLAN_MEIER import KaplanMeier
from ANOVA_ONEWAY import Anova
from NAIVE_BAYES import NaiveBayes

app = Flask(__name__)


charts_info = {
    "pca_scree_eigen": {
        "title": "PCA eigenvalue scree plot",
        "url": "pca_scree_eigenvalues",
    },
    "pca_heatmap_eigen": {
        "title": "PCA eigenvecots heatmap",
        "url": "pca_heatmap_eigenvec",
    },
    "pearson_heatmap": {
        "title": "Pearson Correlation Heatmap",
        "url": "pearson_heatmap",
    },
    "logistic_confusion_matrix": {
        "title": "Logistic Regression Confusion Matrix",
        "url": "logistic_confmat",
    },
    "logistic_roc": {"title": "Logistic Regression ROC", "url": "logistic_roc"},
    "calibration_belt": {"title": "Calibration Belt", "url": "calibration_belt"},
    "kaplan_meier_survival": {
        "title": "Kaplan-Meier Survival Curves",
        "url": "kaplan_meier_survival",
    },
    "anova_errorbars": {"title": "Anova Mean Plot", "url": "anova_errorbars"},
    "naive_bayes_confusion_matrix": {
        "title": "NaiveBayes CM",
        "url": "naive_bayes_confusion_matrix",
    },
    "naive_bayes_roc": {"title": "NaiveBayes ROC", "url": "naive_bayes_roc",},
}


@app.route("/")
@app.route("/home")
def home():
    return render_template(
        "home.html", title="Exareme Highcharts", charts_info=charts_info
    )


@app.route("/anova_errorbars")
def anova_errorbars():
    anova_args = [
        "-y",
        "lefthippocampus",
        "-x",
        "alzheimerbroadcategory",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        "",
    ]
    result = get_algorithm_result(Anova, anova_args)
    result = result["result"][3]["data"]
    return render_template(
        "highchart_layout.html", title="Anova Mean Plot", data=result
    )


@app.route("/pca_scree_eigenvalues")
def pca_scree_eigenvalues():
    pca_args = [
        "-y",
        "subjectage,rightventraldc,rightaccumbensarea, gender",
        "-pathology",
        "dementia, leftaccumbensarea",
        "-dataset",
        "adni",
        "-filter",
        "",
        "-formula",
        "",
        "-coding",
        "Treatment",
    ]
    result = get_algorithm_result(PCA, pca_args)
    result = result["result"][3]["data"]
    return render_template("highchart_layout.html", title="PCA scree plot", data=result)


@app.route("/pca_heatmap_eigenvec")
def pca_heatmap_eigenvec():
    pca_args = [
        "-y",
        "subjectage,rightventraldc,rightaccumbensarea, gender",
        "-pathology",
        "dementia, leftaccumbensarea",
        "-dataset",
        "adni",
        "-filter",
        "",
        "-formula",
        "",
        "-coding",
        "Treatment",
    ]
    result = get_algorithm_result(PCA, pca_args)
    result = result["result"][4]["data"]
    return render_template(
        "highchart_layout.html", title="PCA bubble plot", data=result
    )


@app.route("/pearson_heatmap")
def pearson_heatmap():
    args = [
        "-x",
        "",
        "-y",
        "leftputamen, righthippocampus, subjectage,rightventraldc,rightaccumbensarea, "
        "rightioginferioroccipitalgyrus,rightmfcmedialfrontalcortex, lefthippocampus,"
        "rightppplanumpolare",
        "-pathology",
        "dementia, leftaccumbensarea",
        "-dataset",
        "adni",
        "-filter",
        "",
        "-formula",
        "",
        "-coding",
        "",
    ]
    result = get_algorithm_result(Pearson, args)
    result = result["result"][2]["data"]
    return render_template(
        "highchart_layout.html", title="Pearson Correlation Heatmap", data=result
    )


@app.route("/logistic_confmat")
def logistic_confmat():
    args = [
        "-x",
        "lefthippocampus",
        "-y",
        "alzheimerbroadcategory",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        """
        {
            "condition": "OR",
            "rules": [
                {
                    "id": "alzheimerbroadcategory",
                    "field": "alzheimerbroadcategory",
                    "type": "string",
                    "input": "text",
                    "operator": "equal",
                    "value": "AD"
                },
                {
                    "id": "alzheimerbroadcategory",
                    "field": "alzheimerbroadcategory",
                    "type": "string",
                    "input": "text",
                    "operator": "equal",
                    "value": "CN"
                }
            ],
            "valid": true
        }
        """,
        "-formula",
        "",
    ]
    result = get_algorithm_result(LogisticRegression, args)
    result = result["result"][3]["data"]
    return render_template(
        "highchart_layout.html",
        title="Logistic Regression Confusion Matrix",
        data=result,
    )


@app.route("/logistic_roc")
def logistic_roc():
    args = [
        "-x",
        "lefthippocampus",
        "-y",
        "alzheimerbroadcategory",
        "-pathology",
        "dementia",
        "-dataset",
        "adni",
        "-filter",
        """
        {
            "condition": "OR",
            "rules": [
                {
                    "id": "alzheimerbroadcategory",
                    "field": "alzheimerbroadcategory",
                    "type": "string",
                    "input": "text",
                    "operator": "equal",
                    "value": "AD"
                },
                {
                    "id": "alzheimerbroadcategory",
                    "field": "alzheimerbroadcategory",
                    "type": "string",
                    "input": "text",
                    "operator": "equal",
                    "value": "CN"
                }
            ],
            "valid": true
        }
        """,
        "-formula",
        "",
    ]
    result = get_algorithm_result(LogisticRegression, args)
    result = result["result"][4]["data"]
    return render_template(
        "highchart_layout.html", title="Logistic Regression Confusion ROC", data=result,
    )


@app.route("/calibration_belt")
def calibration_belt():
    args = [
        "-x",
        "probGiViTI_2017_Complessiva",
        "-y",
        "hospOutcomeLatest_RIC10",
        "-devel",
        "internal",
        "-max_deg",
        "4",
        "-confLevels",
        "0.80, 0.95",
        "-thres",
        "0.95",
        "-num_points",
        "200",
        "-pathology",
        "dementia",
        "-dataset",
        "cb_data",
        "-filter",
        "",
        "-formula",
        "",
    ]
    result = get_algorithm_result(CalibrationBelt, args)
    result = result["result"][1]["data"]
    return render_template(
        "highchart_layout.html", title="Calibration Belt", data=result,
    )


@app.route("/kaplan_meier_survival")
def kaplan_meier_survival():
    args = [
        "-x",
        "apoe4",
        "-y",
        "alzheimerbroadcategory",
        "-pathology",
        "dementia",
        "-dataset",
        "alzheimer_fake_cohort",
        "-filter",
        """
        {
            "condition":"OR",
            "rules":[
                {
                    "id":"alzheimerbroadcategory",
                    "field":"alzheimerbroadcategory",
                    "type":"string",
                    "input":"select",
                    "operator":"equal",
                    "value":"AD"
                },
                {
                    "id":"alzheimerbroadcategory",
                    "field":"alzheimerbroadcategory",
                    "type":"string",
                    "input":"select",
                    "operator":"equal",
                    "value":"MCI"
                }
            ],
            "valid":true
        }
        """,
        "-outcome_pos",
        "AD",
        "-outcome_neg",
        "MCI",
        "-total_duration",
        "1100",
    ]
    result = get_algorithm_result(KaplanMeier, args)
    result = result["result"][1]["data"]
    return render_template("highchart_layout.html", title="Kaplan Meier", data=result,)


nb_args = [
    "-x",
    # "lefthippocampus,righthippocampus,leftaccumbensarea",
    # "gender,alzheimerbroadcategory,agegroup",
    "lefthippocampus,righthippocampus,leftaccumbensarea,apoe4,alzheimerbroadcategory",
    "-y",
    "agegroup",
    "-alpha",
    "1",
    "-k",
    "10",
    "-pathology",
    "dementia",
    "-dataset",
    "adni, ppmi",
    "-filter",
    "",
]


@app.route("/naive_bayes_confusion_matrix")
def naive_bayes_confusion_matrix():
    result = get_algorithm_result(NaiveBayes, nb_args)
    result = result["result"][4]["data"]
    return render_template(
        "highchart_layout.html", title="NaiveBayes Confusion Martix", data=result
    )


@app.route("/naive_bayes_roc")
def naive_bayes_roc():
    result = get_algorithm_result(NaiveBayes, nb_args)
    result = result["result"][5]["data"]
    return render_template("highchart_layout.html", title="NaiveBayes ROC", data=result)


if __name__ == "__main__":
    app.run(debug=True)
