import requests
import json
import logging


url='http://localhost:9090/mining/query/ID3'

def test_ID3_1():
    logging.info("---------- TEST : ID3 - Test using contact-lenses dataset  ")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "iterations_condition_query_provided", "value": "true" },
            {   "name": "columns", "value": "CL_age,CL_spectacle_prescrip,CL_astigmatism,CL_tear_prod_rate"  },
            {   "name": "classname", "value": "CL_contact_lenses" },
            {   "name": "dataset", "value": "contact-lenses" },
            {   "name": "filter", "value": "" },
            {   "name": "outputformat", "value": "wekaviewer" }
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(url,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (result)

    # Exareme Result
    # result =[{'no': 0, 'result': 'CL_tear_prod_rate=reduced:none'}, {'no': 1, 'result': 'CL_tear_prod_rate=normal'}, {'no': 2, 'result': '|CL_astigmatism=no'}, {'no': 3, 'result': '||CL_age=pre-presbyopic:soft'}, {'no': 4, 'result': '||CL_age=young:soft'}, {'no': 5, 'result': '||CL_age=presbyopic'}, {'no': 6, 'result': '|||CL_spectacle_prescrip=hypermetrope:soft'}, {'no': 7, 'result': '|||CL_spectacle_prescrip=myope:none'}, {'no': 8, 'result': '|CL_astigmatism=yes'}, {'no': 9, 'result': '||CL_spectacle_prescrip=myope:hard'}, {'no': 10, 'result': '||CL_spectacle_prescrip=hypermetrope'}, {'no': 11, 'result': '|||CL_age=pre-presbyopic:none'}, {'no': 12, 'result': '|||CL_age=presbyopic:none'}, {'no': 13, 'result': '|||CL_age=young:hard'}]


    #  WEKA RESULT
    # # tear-prod-rate = reduced: none
    # # tear-prod-rate = normal
    # # |  astigmatism = no
    # # |  |  age = young: soft
    # # |  |  age = pre-presbyopic: soft
    # # |  |  age = presbyopic
    # # |  |  |  spectacle-prescrip = myope: none
    # # |  |  |  spectacle-prescrip = hypermetrope: soft
    # # |  astigmatism = yes
    # # |  |  spectacle-prescrip = myope: hard
    # # |  |  spectacle-prescrip = hypermetrope
    # # |  |  |  age = young: hard
    # # |  |  |  age = pre-presbyopic: none
    # # |  |  |  age = presbyopic: none

    correctResult  = '''\
CL_tear_prod_rate = reduced: none
CL_tear_prod_rate = normal
|  CL_astigmatism = no
|  |  CL_age = pre-presbyopic: soft
|  |  CL_age = young: soft
|  |  CL_age = presbyopic
|  |  |  CL_spectacle_prescrip = hypermetrope: soft
|  |  |  CL_spectacle_prescrip = myope: none
|  CL_astigmatism = yes
|  |  CL_spectacle_prescrip = myope: hard
|  |  CL_spectacle_prescrip = hypermetrope
|  |  |  CL_age = pre-presbyopic: none
|  |  |  CL_age = presbyopic: none
|  |  |  CL_age = young: hard'''

    check_variable(result,correctResult)
def check_variable(exaremeResult, correctResult):

    exaremeresultfinal=""
    for i in range(len(exaremeResult)):
        exaremeresultfinal += str(exaremeResult[i]['result'])
    correctResult=str(correctResult)
    correctResult = correctResult.replace(' ','')
    correctResult = correctResult.replace('\n','')

    assert (str(exaremeresultfinal) == str(correctResult))

# def test_ID3_2():
#     logging.info("---------- TEST : ID3 - Test using contact-lenses dataset  ")
#
#     data = [
#             {   "name": "iterations_max_number", "value": "500" },
#             {   "name": "iterations_condition_query_provided", "value": "true" },
#             {   "name": "columns", "value": "car_buying, car_maint,car_doors,car_persons,car_lug_boot,car_safety"},
#             {   "name": "classname", "value": "car_class" },
#             {   "name": "dataset", "value": "car" },
#             {   "name": "filter", "value": "" },
#             {   "name": "outputformat", "value": "wekaviewer" }
#         ]
#
#     headers = {'Content-type': 'application/json', "Accept": "text/plain"}
#     r = requests.post(url,data=json.dumps(data),headers=headers)
#     result = json.loads(r.text)
#     print (result)
#
#     # Exareme Result
#     result =[{'no': 0, 'result': 'car_safety=low:unacc'}, {'no': 1, 'result': 'car_safety=high'}, {'no': 2, 'result': '|car_persons=2:unacc'}, {'no': 3, 'result': '|car_persons=4'}, {'no': 4, 'result': '||car_buying=high'}, {'no': 5, 'result': '|||car_maint=high:acc'}, {'no': 6, 'result': '|||car_maint=low:acc'}, {'no': 7, 'result': '|||car_maint=med:acc'}, {'no': 8, 'result': '|||car_maint=vhigh:unacc'}, {'no': 9, 'result': '||car_buying=low'}, {'no': 10, 'result': '|||car_maint=vhigh:acc'}, {'no': 11, 'result': '|||car_maint=high'}, {'no': 12, 'result': '||||car_lug_boot=big:vgood'}, {'no': 13, 'result': '||||car_lug_boot=small:acc'}, {'no': 14, 'result': '||||car_lug_boot=med'}, {'no': 15, 'result': '|||||car_doors=2:acc'}, {'no': 16, 'result': '|||||car_doors=3:acc'}, {'no': 17, 'result': '|||||car_doors=4:vgood'}, {'no': 18, 'result': '|||||car_doors=5more:vgood'}, {'no': 19, 'result': '|||car_maint=low'}, {'no': 20, 'result': '||||car_lug_boot=big:vgood'}, {'no': 21, 'result': '||||car_lug_boot=small:good'}, {'no': 22, 'result': '||||car_lug_boot=med'}, {'no': 23, 'result': '|||||car_doors=2:good'}, {'no': 24, 'result': '|||||car_doors=3:good'}, {'no': 25, 'result': '|||||car_doors=4:vgood'}, {'no': 26, 'result': '|||||car_doors=5more:vgood'}, {'no': 27, 'result': '|||car_maint=med'}, {'no': 28, 'result': '||||car_lug_boot=big:vgood'}, {'no': 29, 'result': '||||car_lug_boot=small:good'}, {'no': 30, 'result': '||||car_lug_boot=med'}, {'no': 31, 'result': '|||||car_doors=2:good'}, {'no': 32, 'result': '|||||car_doors=3:good'}, {'no': 33, 'result': '|||||car_doors=4:vgood'}, {'no': 34, 'result': '|||||car_doors=5more:vgood'}, {'no': 35, 'result': '||car_buying=med'}, {'no': 36, 'result': '|||car_maint=high:acc'}, {'no': 37, 'result': '|||car_maint=vhigh:acc'}, {'no': 38, 'result': '|||car_maint=low'}, {'no': 39, 'result': '||||car_lug_boot=big:vgood'}, {'no': 40, 'result': '||||car_lug_boot=small:good'}, {'no': 41, 'result': '||||car_lug_boot=med'}, {'no': 42, 'result': '|||||car_doors=2:good'}, {'no': 43, 'result': '|||||car_doors=3:good'}, {'no': 44, 'result': '|||||car_doors=4:vgood'}, {'no': 45, 'result': '|||||car_doors=5more:vgood'}, {'no': 46, 'result': '|||car_maint=med'}, {'no': 47, 'result': '||||car_lug_boot=big:vgood'}, {'no': 48, 'result': '||||car_lug_boot=small:acc'}, {'no': 49, 'result': '||||car_lug_boot=med'}, {'no': 50, 'result': '|||||car_doors=2:acc'}, {'no': 51, 'result': '|||||car_doors=3:acc'}, {'no': 52, 'result': '|||||car_doors=4:vgood'}, {'no': 53, 'result': '|||||car_doors=5more:vgood'}, {'no': 54, 'result': '||car_buying=vhigh'}, {'no': 55, 'result': '|||car_maint=high:unacc'}, {'no': 56, 'result': '|||car_maint=low:acc'}, {'no': 57, 'result': '|||car_maint=med:acc'}, {'no': 58, 'result': '|||car_maint=vhigh:unacc'}, {'no': 59, 'result': '|car_persons=more'}, {'no': 60, 'result': '||car_buying=high'}, {'no': 61, 'result': '|||car_maint=vhigh:unacc'}, {'no': 62, 'result': '|||car_maint=high'}, {'no': 63, 'result': '||||car_doors=3:acc'}, {'no': 64, 'result': '||||car_doors=4:acc'}, {'no': 65, 'result': '||||car_doors=5more:acc'}, {'no': 66, 'result': '||||car_doors=2'}, {'no': 67, 'result': '|||||car_lug_boot=big:acc'}, {'no': 68, 'result': '|||||car_lug_boot=med:acc'}, {'no': 69, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 70, 'result': '|||car_maint=low'}, {'no': 71, 'result': '||||car_doors=3:acc'}, {'no': 72, 'result': '||||car_doors=4:acc'}, {'no': 73, 'result': '||||car_doors=5more:acc'}, {'no': 74, 'result': '||||car_doors=2'}, {'no': 75, 'result': '|||||car_lug_boot=big:acc'}, {'no': 76, 'result': '|||||car_lug_boot=med:acc'}, {'no': 77, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 78, 'result': '|||car_maint=med'}, {'no': 79, 'result': '||||car_doors=3:acc'}, {'no': 80, 'result': '||||car_doors=4:acc'}, {'no': 81, 'result': '||||car_doors=5more:acc'}, {'no': 82, 'result': '||||car_doors=2'}, {'no': 83, 'result': '|||||car_lug_boot=big:acc'}, {'no': 84, 'result': '|||||car_lug_boot=med:acc'}, {'no': 85, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 86, 'result': '||car_buying=low'}, {'no': 87, 'result': '|||car_maint=high'}, {'no': 88, 'result': '||||car_lug_boot=big:vgood'}, {'no': 89, 'result': '||||car_lug_boot=med'}, {'no': 90, 'result': '|||||car_doors=2:acc'}, {'no': 91, 'result': '|||||car_doors=3:vgood'}, {'no': 92, 'result': '|||||car_doors=4:vgood'}, {'no': 93, 'result': '|||||car_doors=5more:vgood'}, {'no': 94, 'result': '||||car_lug_boot=small'}, {'no': 95, 'result': '|||||car_doors=2:unacc'}, {'no': 96, 'result': '|||||car_doors=3:acc'}, {'no': 97, 'result': '|||||car_doors=4:acc'}, {'no': 98, 'result': '|||||car_doors=5more:acc'}, {'no': 99, 'result': '|||car_maint=low'}, {'no': 100, 'result': '||||car_lug_boot=big:vgood'}, {'no': 101, 'result': '||||car_lug_boot=med'}, {'no': 102, 'result': '|||||car_doors=2:good'}, {'no': 103, 'result': '|||||car_doors=3:vgood'}, {'no': 104, 'result': '|||||car_doors=4:vgood'}, {'no': 105, 'result': '|||||car_doors=5more:vgood'}, {'no': 106, 'result': '||||car_lug_boot=small'}, {'no': 107, 'result': '|||||car_doors=2:unacc'}, {'no': 108, 'result': '|||||car_doors=3:good'}, {'no': 109, 'result': '|||||car_doors=4:good'}, {'no': 110, 'result': '|||||car_doors=5more:good'}, {'no': 111, 'result': '|||car_maint=med'}, {'no': 112, 'result': '||||car_lug_boot=big:vgood'}, {'no': 113, 'result': '||||car_lug_boot=med'}, {'no': 114, 'result': '|||||car_doors=2:good'}, {'no': 115, 'result': '|||||car_doors=3:vgood'}, {'no': 116, 'result': '|||||car_doors=4:vgood'}, {'no': 117, 'result': '|||||car_doors=5more:vgood'}, {'no': 118, 'result': '||||car_lug_boot=small'}, {'no': 119, 'result': '|||||car_doors=2:unacc'}, {'no': 120, 'result': '|||||car_doors=3:good'}, {'no': 121, 'result': '|||||car_doors=4:good'}, {'no': 122, 'result': '|||||car_doors=5more:good'}, {'no': 123, 'result': '|||car_maint=vhigh'}, {'no': 124, 'result': '||||car_doors=3:acc'}, {'no': 125, 'result': '||||car_doors=4:acc'}, {'no': 126, 'result': '||||car_doors=5more:acc'}, {'no': 127, 'result': '||||car_doors=2'}, {'no': 128, 'result': '|||||car_lug_boot=big:acc'}, {'no': 129, 'result': '|||||car_lug_boot=med:acc'}, {'no': 130, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 131, 'result': '||car_buying=med'}, {'no': 132, 'result': '|||car_maint=high'}, {'no': 133, 'result': '||||car_doors=3:acc'}, {'no': 134, 'result': '||||car_doors=4:acc'}, {'no': 135, 'result': '||||car_doors=5more:acc'}, {'no': 136, 'result': '||||car_doors=2'}, {'no': 137, 'result': '|||||car_lug_boot=big:acc'}, {'no': 138, 'result': '|||||car_lug_boot=med:acc'}, {'no': 139, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 140, 'result': '|||car_maint=low'}, {'no': 141, 'result': '||||car_lug_boot=big:vgood'}, {'no': 142, 'result': '||||car_lug_boot=med'}, {'no': 143, 'result': '|||||car_doors=2:good'}, {'no': 144, 'result': '|||||car_doors=3:vgood'}, {'no': 145, 'result': '|||||car_doors=4:vgood'}, {'no': 146, 'result': '|||||car_doors=5more:vgood'}, {'no': 147, 'result': '||||car_lug_boot=small'}, {'no': 148, 'result': '|||||car_doors=2:unacc'}, {'no': 149, 'result': '|||||car_doors=3:good'}, {'no': 150, 'result': '|||||car_doors=4:good'}, {'no': 151, 'result': '|||||car_doors=5more:good'}, {'no': 152, 'result': '|||car_maint=med'}, {'no': 153, 'result': '||||car_lug_boot=big:vgood'}, {'no': 154, 'result': '||||car_lug_boot=med'}, {'no': 155, 'result': '|||||car_doors=2:acc'}, {'no': 156, 'result': '|||||car_doors=3:vgood'}, {'no': 157, 'result': '|||||car_doors=4:vgood'}, {'no': 158, 'result': '|||||car_doors=5more:vgood'}, {'no': 159, 'result': '||||car_lug_boot=small'}, {'no': 160, 'result': '|||||car_doors=2:unacc'}, {'no': 161, 'result': '|||||car_doors=3:acc'}, {'no': 162, 'result': '|||||car_doors=4:acc'}, {'no': 163, 'result': '|||||car_doors=5more:acc'}, {'no': 164, 'result': '|||car_maint=vhigh'}, {'no': 165, 'result': '||||car_doors=3:acc'}, {'no': 166, 'result': '||||car_doors=4:acc'}, {'no': 167, 'result': '||||car_doors=5more:acc'}, {'no': 168, 'result': '||||car_doors=2'}, {'no': 169, 'result': '|||||car_lug_boot=big:acc'}, {'no': 170, 'result': '|||||car_lug_boot=med:acc'}, {'no': 171, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 172, 'result': '||car_buying=vhigh'}, {'no': 173, 'result': '|||car_maint=high:unacc'}, {'no': 174, 'result': '|||car_maint=vhigh:unacc'}, {'no': 175, 'result': '|||car_maint=low'}, {'no': 176, 'result': '||||car_doors=3:acc'}, {'no': 177, 'result': '||||car_doors=4:acc'}, {'no': 178, 'result': '||||car_doors=5more:acc'}, {'no': 179, 'result': '||||car_doors=2'}, {'no': 180, 'result': '|||||car_lug_boot=big:acc'}, {'no': 181, 'result': '|||||car_lug_boot=med:acc'}, {'no': 182, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 183, 'result': '|||car_maint=med'}, {'no': 184, 'result': '||||car_doors=3:acc'}, {'no': 185, 'result': '||||car_doors=4:acc'}, {'no': 186, 'result': '||||car_doors=5more:acc'}, {'no': 187, 'result': '||||car_doors=2'}, {'no': 188, 'result': '|||||car_lug_boot=big:acc'}, {'no': 189, 'result': '|||||car_lug_boot=med:acc'}, {'no': 190, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 191, 'result': 'car_safety=med'}, {'no': 192, 'result': '|car_persons=2:unacc'}, {'no': 193, 'result': '|car_persons=4'}, {'no': 194, 'result': '||car_buying=high'}, {'no': 195, 'result': '|||car_lug_boot=small:unacc'}, {'no': 196, 'result': '|||car_lug_boot=big'}, {'no': 197, 'result': '||||car_maint=high:acc'}, {'no': 198, 'result': '||||car_maint=low:acc'}, {'no': 199, 'result': '||||car_maint=med:acc'}, {'no': 200, 'result': '||||car_maint=vhigh:unacc'}, {'no': 201, 'result': '|||car_lug_boot=med'}, {'no': 202, 'result': '||||car_doors=2:unacc'}, {'no': 203, 'result': '||||car_doors=3:unacc'}, {'no': 204, 'result': '||||car_doors=4'}, {'no': 205, 'result': '|||||car_maint=high:acc'}, {'no': 206, 'result': '|||||car_maint=low:acc'}, {'no': 207, 'result': '|||||car_maint=med:acc'}, {'no': 208, 'result': '|||||car_maint=vhigh:unacc'}, {'no': 209, 'result': '||||car_doors=5more'}, {'no': 210, 'result': '|||||car_maint=high:acc'}, {'no': 211, 'result': '|||||car_maint=low:acc'}, {'no': 212, 'result': '|||||car_maint=med:acc'}, {'no': 213, 'result': '|||||car_maint=vhigh:unacc'}, {'no': 214, 'result': '||car_buying=low'}, {'no': 215, 'result': '|||car_maint=high:acc'}, {'no': 216, 'result': '|||car_maint=low'}, {'no': 217, 'result': '||||car_lug_boot=big:good'}, {'no': 218, 'result': '||||car_lug_boot=small:acc'}, {'no': 219, 'result': '||||car_lug_boot=med'}, {'no': 220, 'result': '|||||car_doors=2:acc'}, {'no': 221, 'result': '|||||car_doors=3:acc'}, {'no': 222, 'result': '|||||car_doors=4:good'}, {'no': 223, 'result': '|||||car_doors=5more:good'}, {'no': 224, 'result': '|||car_maint=med'}, {'no': 225, 'result': '||||car_lug_boot=big:good'}, {'no': 226, 'result': '||||car_lug_boot=small:acc'}, {'no': 227, 'result': '||||car_lug_boot=med'}, {'no': 228, 'result': '|||||car_doors=2:acc'}, {'no': 229, 'result': '|||||car_doors=3:acc'}, {'no': 230, 'result': '|||||car_doors=4:good'}, {'no': 231, 'result': '|||||car_doors=5more:good'}, {'no': 232, 'result': '|||car_maint=vhigh'}, {'no': 233, 'result': '||||car_lug_boot=big:acc'}, {'no': 234, 'result': '||||car_lug_boot=small:unacc'}, {'no': 235, 'result': '||||car_lug_boot=med'}, {'no': 236, 'result': '|||||car_doors=2:unacc'}, {'no': 237, 'result': '|||||car_doors=3:unacc'}, {'no': 238, 'result': '|||||car_doors=4:acc'}, {'no': 239, 'result': '|||||car_doors=5more:acc'}, {'no': 240, 'result': '||car_buying=med'}, {'no': 241, 'result': '|||car_maint=med:acc'}, {'no': 242, 'result': '|||car_maint=high'}, {'no': 243, 'result': '||||car_lug_boot=big:acc'}, {'no': 244, 'result': '||||car_lug_boot=small:unacc'}, {'no': 245, 'result': '||||car_lug_boot=med'}, {'no': 246, 'result': '|||||car_doors=2:unacc'}, {'no': 247, 'result': '|||||car_doors=3:unacc'}, {'no': 248, 'result': '|||||car_doors=4:acc'}, {'no': 249, 'result': '|||||car_doors=5more:acc'}, {'no': 250, 'result': '|||car_maint=low'}, {'no': 251, 'result': '||||car_lug_boot=big:good'}, {'no': 252, 'result': '||||car_lug_boot=small:acc'}, {'no': 253, 'result': '||||car_lug_boot=med'}, {'no': 254, 'result': '|||||car_doors=2:acc'}, {'no': 255, 'result': '|||||car_doors=3:acc'}, {'no': 256, 'result': '|||||car_doors=4:good'}, {'no': 257, 'result': '|||||car_doors=5more:good'}, {'no': 258, 'result': '|||car_maint=vhigh'}, {'no': 259, 'result': '||||car_lug_boot=big:acc'}, {'no': 260, 'result': '||||car_lug_boot=small:unacc'}, {'no': 261, 'result': '||||car_lug_boot=med'}, {'no': 262, 'result': '|||||car_doors=2:unacc'}, {'no': 263, 'result': '|||||car_doors=3:unacc'}, {'no': 264, 'result': '|||||car_doors=4:acc'}, {'no': 265, 'result': '|||||car_doors=5more:acc'}, {'no': 266, 'result': '||car_buying=vhigh'}, {'no': 267, 'result': '|||car_maint=high:unacc'}, {'no': 268, 'result': '|||car_maint=vhigh:unacc'}, {'no': 269, 'result': '|||car_maint=low'}, {'no': 270, 'result': '||||car_lug_boot=big:acc'}, {'no': 271, 'result': '||||car_lug_boot=small:unacc'}, {'no': 272, 'result': '||||car_lug_boot=med'}, {'no': 273, 'result': '|||||car_doors=2:unacc'}, {'no': 274, 'result': '|||||car_doors=3:unacc'}, {'no': 275, 'result': '|||||car_doors=4:acc'}, {'no': 276, 'result': '|||||car_doors=5more:acc'}, {'no': 277, 'result': '|||car_maint=med'}, {'no': 278, 'result': '||||car_lug_boot=big:acc'}, {'no': 279, 'result': '||||car_lug_boot=small:unacc'}, {'no': 280, 'result': '||||car_lug_boot=med'}, {'no': 281, 'result': '|||||car_doors=2:unacc'}, {'no': 282, 'result': '|||||car_doors=3:unacc'}, {'no': 283, 'result': '|||||car_doors=4:acc'}, {'no': 284, 'result': '|||||car_doors=5more:acc'}, {'no': 285, 'result': '|car_persons=more'}, {'no': 286, 'result': '||car_buying=high'}, {'no': 287, 'result': '|||car_lug_boot=small:unacc'}, {'no': 288, 'result': '|||car_lug_boot=big'}, {'no': 289, 'result': '||||car_maint=high:acc'}, {'no': 290, 'result': '||||car_maint=low:acc'}, {'no': 291, 'result': '||||car_maint=med:acc'}, {'no': 292, 'result': '||||car_maint=vhigh:unacc'}, {'no': 293, 'result': '|||car_lug_boot=med'}, {'no': 294, 'result': '||||car_doors=2:unacc'}, {'no': 295, 'result': '||||car_doors=3'}, {'no': 296, 'result': '|||||car_maint=high:acc'}, {'no': 297, 'result': '|||||car_maint=low:acc'}, {'no': 298, 'result': '|||||car_maint=med:acc'}, {'no': 299, 'result': '|||||car_maint=vhigh:unacc'}, {'no': 300, 'result': '||||car_doors=4'}, {'no': 301, 'result': '|||||car_maint=high:acc'}, {'no': 302, 'result': '|||||car_maint=low:acc'}, {'no': 303, 'result': '|||||car_maint=med:acc'}, {'no': 304, 'result': '|||||car_maint=vhigh:unacc'}, {'no': 305, 'result': '||||car_doors=5more'}, {'no': 306, 'result': '|||||car_maint=high:acc'}, {'no': 307, 'result': '|||||car_maint=low:acc'}, {'no': 308, 'result': '|||||car_maint=med:acc'}, {'no': 309, 'result': '|||||car_maint=vhigh:unacc'}, {'no': 310, 'result': '||car_buying=low'}, {'no': 311, 'result': '|||car_maint=high'}, {'no': 312, 'result': '||||car_doors=3:acc'}, {'no': 313, 'result': '||||car_doors=4:acc'}, {'no': 314, 'result': '||||car_doors=5more:acc'}, {'no': 315, 'result': '||||car_doors=2'}, {'no': 316, 'result': '|||||car_lug_boot=big:acc'}, {'no': 317, 'result': '|||||car_lug_boot=med:acc'}, {'no': 318, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 319, 'result': '|||car_maint=low'}, {'no': 320, 'result': '||||car_lug_boot=big:good'}, {'no': 321, 'result': '||||car_lug_boot=med'}, {'no': 322, 'result': '|||||car_doors=2:acc'}, {'no': 323, 'result': '|||||car_doors=3:good'}, {'no': 324, 'result': '|||||car_doors=4:good'}, {'no': 325, 'result': '|||||car_doors=5more:good'}, {'no': 326, 'result': '||||car_lug_boot=small'}, {'no': 327, 'result': '|||||car_doors=2:unacc'}, {'no': 328, 'result': '|||||car_doors=3:acc'}, {'no': 329, 'result': '|||||car_doors=4:acc'}, {'no': 330, 'result': '|||||car_doors=5more:acc'}, {'no': 331, 'result': '|||car_maint=med'}, {'no': 332, 'result': '||||car_lug_boot=big:good'}, {'no': 333, 'result': '||||car_lug_boot=med'}, {'no': 334, 'result': '|||||car_doors=2:acc'}, {'no': 335, 'result': '|||||car_doors=3:good'}, {'no': 336, 'result': '|||||car_doors=4:good'}, {'no': 337, 'result': '|||||car_doors=5more:good'}, {'no': 338, 'result': '||||car_lug_boot=small'}, {'no': 339, 'result': '|||||car_doors=2:unacc'}, {'no': 340, 'result': '|||||car_doors=3:acc'}, {'no': 341, 'result': '|||||car_doors=4:acc'}, {'no': 342, 'result': '|||||car_doors=5more:acc'}, {'no': 343, 'result': '|||car_maint=vhigh'}, {'no': 344, 'result': '||||car_lug_boot=big:acc'}, {'no': 345, 'result': '||||car_lug_boot=small:unacc'}, {'no': 346, 'result': '||||car_lug_boot=med'}, {'no': 347, 'result': '|||||car_doors=2:unacc'}, {'no': 348, 'result': '|||||car_doors=3:acc'}, {'no': 349, 'result': '|||||car_doors=4:acc'}, {'no': 350, 'result': '|||||car_doors=5more:acc'}, {'no': 351, 'result': '||car_buying=med'}, {'no': 352, 'result': '|||car_maint=high'}, {'no': 353, 'result': '||||car_lug_boot=big:acc'}, {'no': 354, 'result': '||||car_lug_boot=small:unacc'}, {'no': 355, 'result': '||||car_lug_boot=med'}, {'no': 356, 'result': '|||||car_doors=2:unacc'}, {'no': 357, 'result': '|||||car_doors=3:acc'}, {'no': 358, 'result': '|||||car_doors=4:acc'}, {'no': 359, 'result': '|||||car_doors=5more:acc'}, {'no': 360, 'result': '|||car_maint=low'}, {'no': 361, 'result': '||||car_lug_boot=big:good'}, {'no': 362, 'result': '||||car_lug_boot=med'}, {'no': 363, 'result': '|||||car_doors=2:acc'}, {'no': 364, 'result': '|||||car_doors=3:good'}, {'no': 365, 'result': '|||||car_doors=4:good'}, {'no': 366, 'result': '|||||car_doors=5more:good'}, {'no': 367, 'result': '||||car_lug_boot=small'}, {'no': 368, 'result': '|||||car_doors=2:unacc'}, {'no': 369, 'result': '|||||car_doors=3:acc'}, {'no': 370, 'result': '|||||car_doors=4:acc'}, {'no': 371, 'result': '|||||car_doors=5more:acc'}, {'no': 372, 'result': '|||car_maint=med'}, {'no': 373, 'result': '||||car_doors=3:acc'}, {'no': 374, 'result': '||||car_doors=4:acc'}, {'no': 375, 'result': '||||car_doors=5more:acc'}, {'no': 376, 'result': '||||car_doors=2'}, {'no': 377, 'result': '|||||car_lug_boot=big:acc'}, {'no': 378, 'result': '|||||car_lug_boot=med:acc'}, {'no': 379, 'result': '|||||car_lug_boot=small:unacc'}, {'no': 380, 'result': '|||car_maint=vhigh'}, {'no': 381, 'result': '||||car_lug_boot=big:acc'}, {'no': 382, 'result': '||||car_lug_boot=small:unacc'}, {'no': 383, 'result': '||||car_lug_boot=med'}, {'no': 384, 'result': '|||||car_doors=2:unacc'}, {'no': 385, 'result': '|||||car_doors=3:acc'}, {'no': 386, 'result': '|||||car_doors=4:acc'}, {'no': 387, 'result': '|||||car_doors=5more:acc'}, {'no': 388, 'result': '||car_buying=vhigh'}, {'no': 389, 'result': '|||car_maint=high:unacc'}, {'no': 390, 'result': '|||car_maint=vhigh:unacc'}, {'no': 391, 'result': '|||car_maint=low'}, {'no': 392, 'result': '||||car_lug_boot=big:acc'}, {'no': 393, 'result': '||||car_lug_boot=small:unacc'}, {'no': 394, 'result': '||||car_lug_boot=med'}, {'no': 395, 'result': '|||||car_doors=2:unacc'}, {'no': 396, 'result': '|||||car_doors=3:acc'}, {'no': 397, 'result': '|||||car_doors=4:acc'}, {'no': 398, 'result': '|||||car_doors=5more:acc'}, {'no': 399, 'result': '|||car_maint=med'}, {'no': 400, 'result': '||||car_lug_boot=big:acc'}, {'no': 401, 'result': '||||car_lug_boot=small:unacc'}, {'no': 402, 'result': '||||car_lug_boot=med'}, {'no': 403, 'result': '|||||car_doors=2:unacc'}, {'no': 404, 'result': '|||||car_doors=3:acc'}, {'no': 405, 'result': '|||||car_doors=4:acc'}, {'no': 406, 'result': '|||||car_doors=5more:acc'}]


# #
#   correctResult  = '''\
# safety = low: unacc
# safety = med
# |  persons = 2: unacc
# |  persons = 4
# |  |  buying = vhigh
# |  |  |  maint = vhigh: unacc
# |  |  |  maint = high: unacc
# |  |  |  maint = med
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: unacc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  |  maint = low
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: unacc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  buying = high
# |  |  |  lug_boot = small: unacc
# |  |  |  lug_boot = med
# |  |  |  |  doors = 2: unacc
# |  |  |  |  doors = 3: unacc
# |  |  |  |  doors = 4
# |  |  |  |  |  maint = vhigh: unacc
# |  |  |  |  |  maint = high: acc
# |  |  |  |  |  maint = med: acc
# |  |  |  |  |  maint = low: acc
# |  |  |  |  doors = 5more
# |  |  |  |  |  maint = vhigh: unacc
# |  |  |  |  |  maint = high: acc
# |  |  |  |  |  maint = med: acc
# |  |  |  |  |  maint = low: acc
# |  |  |  lug_boot = big
# |  |  |  |  maint = vhigh: unacc
# |  |  |  |  maint = high: acc
# |  |  |  |  maint = med: acc
# |  |  |  |  maint = low: acc
# |  |  buying = med
# |  |  |  maint = vhigh
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: unacc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  |  maint = high
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: unacc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  |  maint = med: acc
# |  |  |  maint = low
# |  |  |  |  lug_boot = small: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: good
# |  |  |  |  |  doors = 5more: good
# |  |  |  |  lug_boot = big: good
# |  |  buying = low
# |  |  |  maint = vhigh
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: unacc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  |  maint = high: acc
# |  |  |  maint = med
# |  |  |  |  lug_boot = small: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: good
# |  |  |  |  |  doors = 5more: good
# |  |  |  |  lug_boot = big: good
# |  |  |  maint = low
# |  |  |  |  lug_boot = small: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: good
# |  |  |  |  |  doors = 5more: good
# |  |  |  |  lug_boot = big: good
# |  persons = more
# |  |  buying = vhigh
# |  |  |  maint = vhigh: unacc
# |  |  |  maint = high: unacc
# |  |  |  maint = med
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  |  maint = low
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  buying = high
# |  |  |  lug_boot = small: unacc
# |  |  |  lug_boot = med
# |  |  |  |  maint = vhigh: unacc
# |  |  |  |  maint = high
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  maint = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  maint = low
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  lug_boot = big
# |  |  |  |  maint = vhigh: unacc
# |  |  |  |  maint = high: acc
# |  |  |  |  maint = med: acc
# |  |  |  |  maint = low: acc
# |  |  buying = med
# |  |  |  maint = vhigh
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  |  maint = high
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  |  maint = med
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  |  maint = low
# |  |  |  |  lug_boot = small
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: good
# |  |  |  |  |  doors = 4: good
# |  |  |  |  |  doors = 5more: good
# |  |  |  |  lug_boot = big: good
# |  |  buying = low
# |  |  |  maint = vhigh
# |  |  |  |  lug_boot = small: unacc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = big: acc
# |  |  |  maint = high
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  |  maint = med
# |  |  |  |  lug_boot = small
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: good
# |  |  |  |  |  doors = 4: good
# |  |  |  |  |  doors = 5more: good
# |  |  |  |  lug_boot = big: good
# |  |  |  maint = low
# |  |  |  |  lug_boot = small
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: good
# |  |  |  |  |  doors = 4: good
# |  |  |  |  |  doors = 5more: good
# |  |  |  |  lug_boot = big: good
# safety = high
# |  persons = 2: unacc
# |  persons = 4
# |  |  buying = vhigh
# |  |  |  maint = vhigh: unacc
# |  |  |  maint = high: unacc
# |  |  |  maint = med: acc
# |  |  |  maint = low: acc
# |  |  buying = high
# |  |  |  maint = vhigh: unacc
# |  |  |  maint = high: acc
# |  |  |  maint = med: acc
# |  |  |  maint = low: acc
# |  |  buying = med
# |  |  |  maint = vhigh: acc
# |  |  |  maint = high: acc
# |  |  |  maint = med
# |  |  |  |  lug_boot = small: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood
# |  |  |  maint = low
# |  |  |  |  lug_boot = small: good
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: good
# |  |  |  |  |  doors = 3: good
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood
# |  |  buying = low
# |  |  |  maint = vhigh: acc
# |  |  |  maint = high
# |  |  |  |  lug_boot = small: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood
# |  |  |  maint = med
# |  |  |  |  lug_boot = small: good
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: good
# |  |  |  |  |  doors = 3: good
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood
# |  |  |  maint = low
# |  |  |  |  lug_boot = small: good
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: good
# |  |  |  |  |  doors = 3: good
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood
# |  persons = more
# |  |  buying = vhigh
# |  |  |  maint = vhigh: unacc
# |  |  |  maint = high: unacc
# |  |  |  maint = med
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  |  maint = low
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  buying = high
# |  |  |  maint = vhigh: unacc
# |  |  |  maint = high
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  |  maint = med
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  |  maint = low
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  buying = med
# |  |  |  maint = vhigh
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  |  maint = high
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  |  maint = med
# |  |  |  |  lug_boot = small
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: vgood
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood
# |  |  |  maint = low
# |  |  |  |  lug_boot = small
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: good
# |  |  |  |  |  doors = 4: good
# |  |  |  |  |  doors = 5more: good
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: good
# |  |  |  |  |  doors = 3: vgood
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood
# |  |  buying = low
# |  |  |  maint = vhigh
# |  |  |  |  doors = 2
# |  |  |  |  |  lug_boot = small: unacc
# |  |  |  |  |  lug_boot = med: acc
# |  |  |  |  |  lug_boot = big: acc
# |  |  |  |  doors = 3: acc
# |  |  |  |  doors = 4: acc
# |  |  |  |  doors = 5more: acc
# |  |  |  maint = high
# |  |  |  |  lug_boot = small
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: acc
# |  |  |  |  |  doors = 4: acc
# |  |  |  |  |  doors = 5more: acc
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: acc
# |  |  |  |  |  doors = 3: vgood
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood
# |  |  |  maint = med
# |  |  |  |  lug_boot = small
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: good
# |  |  |  |  |  doors = 4: good
# |  |  |  |  |  doors = 5more: good
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: good
# |  |  |  |  |  doors = 3: vgood
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood
# |  |  |  maint = low
# |  |  |  |  lug_boot = small
# |  |  |  |  |  doors = 2: unacc
# |  |  |  |  |  doors = 3: good
# |  |  |  |  |  doors = 4: good
# |  |  |  |  |  doors = 5more: good
# |  |  |  |  lug_boot = med
# |  |  |  |  |  doors = 2: good
# |  |  |  |  |  doors = 3: vgood
# |  |  |  |  |  doors = 4: vgood
# |  |  |  |  |  doors = 5more: vgood
# |  |  |  |  lug_boot = big: vgood'''
#
#     check_variable(result,correctResult)
#
#
#
#
#

