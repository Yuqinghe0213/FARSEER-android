import hug
from hug_middleware_cors import CORSMiddleware
import json
import pyodbc

api = hug.API(__name__)
api.http.add_middleware(CORSMiddleware(api))

def connect():
        server = 'farseer-server.database.windows.net'
        database = 'farseer-db'
        username = 'farseer-admin'
        password = 'telstra12b822!'
        cnxn = pyodbc.connect('DRIVER={ODBC Driver 13 for SQL Server};SERVER='+server+';DATABASE='+database+';UID='+username+';PWD='+ password)
        return cnxn
        
@hug.post('/register')
def register(User_Name: hug.types.text, User_PSW: hug.types.text, User_Mobile: hug.types.text, User_Email: hug.types.text):

        cnxn = connect()
        cursor = cnxn.cursor()
        cursor.execute("select * from [dbo].[User] where UserEmail = ?" , User_Email)
        if cursor.rowcount == 0:
                User_Img = pyodbc.Binary()
                #User_Login = 1

                with cursor.execute("insert into [dbo].[User] (UserName, UserPSW, UserIMG, UserMobile, UserEmail) values (?, ?, ?, ?, ?)", (User_Name, User_PSW, User_Img, User_Mobile, User_Email)):
                        # print(json.dumps({"result": 'OK'}))
                        return json.dumps({"result": 'OK'})
        else:
                # print(json.dumps({"result": 'user already exist'}))
                return json.dumps({"result": 'user already exist'})
        cnxn.close()

@hug.post('/login')
def login(User_Email: hug.types.text, User_PSW: hug.types.text):

        json_obj1 = json.dumps({u"result": 'failed, user is not exist'})
        json_obj2 = json.dumps({u"result": 'failed, password is incorrect'})
        json_obj3 = json.dumps({u"result": 'OK'})
        #json_obj4 = json.dumps({u"result": 'failed, user already login'})

        cnxn = connect()
        cursor = cnxn.cursor()

        if cursor.execute("select * from [dbo].[User] where UserEmail= ?", User_Email).rowcount == 0:
                # print(json_obj1)
                return json_obj1
        else:
                cursor.execute("select * from [dbo].[User] where UserEmail= ? and UserPSW=?", (User_Email,User_PSW))
                if cursor.rowcount == 0:
                        # print(json_obj2)
                        return json_obj2
                else:
                        # print(json_obj3)
                        return json_obj3
        cnxn.close()

@hug.get('/item')
def getUser(User_Email: hug.types.text):

        cnxn = connect()
        cursor = cnxn.cursor()

        cursor.execute("select * from [dbo].[User] where UserEmail =?", User_Email)
        list = cursor.fetchone()
        UserName = list[0]
        UserPSW = list[1]
        UserIMG = list[2]
        UserMobile = list[3]
        UserEmail = list[4]
        json_obj = json.dumps({"result": "OK",
                                       "username": UserName,
                                       "userpsw": UserPSW,
                                       "userimg": UserIMG,
                                       "usermobile":UserMobile,
                                       "useremail":UserEmail})

        print (json_obj)
        return json_obj
        cnxn.close()

#@hug.post('/logout')
#def logout(User_Email: hug.types.text):

#        json_obj = json.dumps({u"result": 'OK'})

#        cnxn = connect()
#        cursor = cnxn.cursor()

#        with cursor.execute("update [dbo].[User] set UserLogin = 0 where UserEmail= ?", User_Email):
#             # print(json_obj)
#             return json_obj


# if __name__ == '__main__':
#         getUser('karen@gmail.com')
#         register('ching33333', '123456', '0478171327', 'ching33333@gmail.com')