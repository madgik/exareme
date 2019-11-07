import requests
import json
import numpy as np

endpointUrl = 'http://localhost:9090/mining/query/CALIBRATION_BELT'
headers = {'Content-type': 'application/json', "Accept": "text/plain"}

html_head = """
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <title>
      Chart
    </title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <script src="https://code.highcharts.com/highcharts.js"></script>
    <script src="https://code.highcharts.com/modules/heatmap.js"></script>
    <script src="https://code.highcharts.com/modules/exporting.js"></script>
    <script src="https://code.highcharts.com/modules/series-label.js"></script>
    <script src="https://code.highcharts.com/highcharts-more.js"></script>
    <script src="https://code.highcharts.com/modules/annotations.js"></script>
    <script type="text/javascript">
      document.addEventListener("DOMContentLoaded", function() {
"""
html_tail = """
        var chart = Highcharts.chart("container", {
          title: {
            text: "GiViTI Calibration Belt"
          },

        annotations: [
            {
              labels: [
                {
                  point: { x: 100, y: 100 },
                  text: "Polynomial degree: " + model_deg + " <br/>p-value: p_val <br/>n: " + n,
                  padding: 10,
                  shape: 'rect'
                }
              ],
              labelOptions: {
                borderRadius: 5,
                backgroundColor: "#bbd9fa",
                borderWidth: 1,
                borderColor: "#9aa2ab"
              }
            },
            {
              labels: [
                {
                  point: { x: 400, y: 400 },
                  text: "Confidence level: 80%<br/>Under the bisector: " + under80 + "<br/>Over the bisector: " + 
                  over80,
                  padding: 10,
                  shape: 'rect'
                }
              ],
              labelOptions: {
                borderRadius: 5,
                backgroundColor: "#6e7d8f",
                borderWidth: 1,
                borderColor: "#AAA"
              }
            },
            {
              labels: [
                {
                  point: { x: 400, y: 465 },
                  text: "Confidence level: 95%<br/>Under the bisector: " + under95 + "<br/>Over the bisector: " + 
                  over95,
                  padding: 10,
                  shape: 'rect'
                }
              ],
              labelOptions: {
                borderRadius: 5,
                backgroundColor: "#a5b4c7",
                borderWidth: 1,
                borderColor: "#AAA"
              }
            }
          ],

          xAxis: {
            title: {
              text: "Expected mortality"
            },
            visible: true
          },

          yAxis: {
            title: {
              text: "Observed mortality"
            },
            visible: true
          },

          tooltip: {
            crosshairs: true,
            shared: true
          },

          legend: {},

          series: [
            {
              name: "Calibration curve",
              data: curve,
              zIndex: 3,
              lineWidth: 3,
              color: Highcharts.getOptions().colors[0],
              marker: {
                enabled: false
              },
              label: {
                enabled: false
              }
            },
            {
              name: "Confidence level 95%",
              data: ranges95,
              type: "arearange",
              lineWidth: 0,
              linkedTo: ":previous",
              color: "#a5b4c7",
              zIndex: 0,
              marker: {
                enabled: false
              }
            },
            {
              name: "Confidence level 80%",
              data: ranges80,
              type: "arearange",
              lineWidth: 0,
              linkedTo: ":previous",
              color: "#6e7d8f",
              zIndex: 1,
              marker: {
                enabled: false
              }
            },
            {
              name: "Bisector",
              data: [[0, 0], [1, 1]],
              zIndex: 2,
              color: '#fc7938',
              lineWidth: 1.5,
              dashStyle: "Dash",
              allowPointSelect: false,
              marker: {
                enabled: false
              },
              label: {
                enabled: false
              }
            }
          ]
        });
      });
    </script>
  </head>
  <body>
    <center>
      <div
        id="container"
        style="width: 600px; height: 600px; margin: 10 auto"
      ></div>
    </center>
  </body>
</html>
"""


def get_result():
    data = [
        {"name": "e", "value": "probGiViTI_2017_Complessiva"},
        {"name": "o", "value": "hospOutcomeLatest_RIC10"},
        {"name": "max_deg", "value": "4"},
        {"name": "dataset", "value": "cb_data"},
        {"name": "filter", "value": ""},
        {"name": "max_iter", "value": "20"},
        {"name": "pathology", "value": "dementia"}
    ]
    r = requests.post(endpointUrl, data=json.dumps(data), headers=headers)
    result = json.loads(r.text)
    calib_curve = result['result'][0]['data'][0]['Calibration curve']
    calib_belt95 = result['result'][0]['data'][0]['Calibration belt 95%']
    calib_belt80 = result['result'][0]['data'][0]['Calibration belt 80%']
    n_obs = result['result'][0]['data'][0]['n_obs']
    model_deg = result['result'][0]['data'][0]['Model Parameters']['Model degree']
    p_values = result['result'][0]['data'][0]['p values']
    return calib_curve, calib_belt80, calib_belt95, n_obs, model_deg, p_values


if __name__ == '__main__':
    calib_curve, calib_belt80, calib_belt95, n_obs, model_deg, p_values = get_result()
    html_page = html_head \
                + '\nvar curve = ' + str(calib_curve) + ';' \
                + '\nvar ranges80 = ' + str(calib_belt80) + ';' \
                + '\nvar ranges95 = ' + str(calib_belt95) + ';' \
                + '\nvar model_deg = ' + str(int(model_deg)) + ';' \
                + '\nvar n = ' + str(int(n_obs)) + ';' \
                + '\nvar p_val = ' + str(p_values) + ';' \
                + '\nvar under80 = ' + '\'NEVER\'' + ';' \
                + '\nvar over80 = ' + '\'NEVER\'' + ';' \
                + '\nvar under95 = ' + '\'NEVER\'' + ';' \
                + '\nvar over95 = ' + '\'NEVER\'' + ';' \
                + html_tail
    # print(html_page)
    with open('calibration_belt.html', 'w') as f:
        f.write(html_page)
