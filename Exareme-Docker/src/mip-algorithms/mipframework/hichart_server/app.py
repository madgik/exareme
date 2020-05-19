from flask import Flask, render_template

from mipframework.hichart_server.algorun import get_algorithm_result

from PCA import PCA
from PEARSON_CORRELATION import Pearson
from LOGISTIC_REGRESSION import LogisticRegression
from CALIBRATION_BELT import CalibrationBelt

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
    "logistic_roc": {"title": "Logistic Regression ROC", "url": "logistic_roc",},
    "calibration_belt": {"title": "Calibration Belt", "url": "calibration_belt"},
}


@app.route("/")
@app.route("/home")
def home():
    return render_template(
        "home.html", title="Exareme Highcharts", charts_info=charts_info
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


if __name__ == "__main__":
    app.run(debug=True)
