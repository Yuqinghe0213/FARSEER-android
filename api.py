
# our backend is based on Azure VPS. 
# Specifically, we created an Ubuntu Virtual Machine and a sql server
# hug is the cleanest way to create HTTP REST APIs on Python3. 
# It consistently benchmarks among the top 3 performing web frameworks for Python, 
# handily beating out Flask and Django. For almost every common Web API task the 
# code written to accomplish it in hug is a small fraction of what is required in 
# other Frameworks.

import hug
from hug_middleware_cors import CORSMiddleware
import json
import pyodbc
from decimal import Decimal

api = hug.API(__name__)
api.http.add_middleware(CORSMiddleware(api))

# build connection to DB server
def connect():
	server = 'farseer-server.database.windows.net'
	database = 'farseer-db'
	username = 'farseer-admin'
	password = 'telstra12b822!'
	driver = '{ODBC Driver 13 for SQL Server}'
	cnxn = pyodbc.connect('DRIVER='+driver+';SERVER='+server+';DATABASE='+database+';UID='+username+';PWD='+password)
	return cnxn

# post user register information to DB when user sign up
@hug.post('/register')
def register(User_Name: hug.types.text, User_PSW: hug.types.text, User_Mobile: hug.types.text, User_Email: hug.types.text):
	cnxn = connect()
	cursor = cnxn.cursor()
	cursor.execute("select * from [dbo].[Users] where UserEmail = ?", User_Email)
	if cursor.rowcount == 0:
		User_IMG = pyodbc.Binary()
		User_Login = 1
		with cursor.execute("insert into [dbo].[Users] (UserName, UserPSW, UserIMG,UserMobile, UserEmail, UserLogin) values (?,?,?,?,?,?)", (User_Name, User_PSW, User_IMG, User_Mobile, User_Email, User_Login)):
			print(json.dumps({u"result":'OK'}))
			return json.dumps({u"result":'OK'})
	else:
		return json.dumps({u"result":'user already exist'})
	cnxn.close()

# post user login information to DB when user login and 
# confirm whether the login info is correct or not
@hug.post('/login')
def login(User_Email: hug.types.text, User_PSW: hug.types.text):
	
	json_obj1 = json.dumps({u"result": 'failed, user is not exist'})
	json_obj2 = json.dumps({u"result": 'failed, password is incorrect'})
	json_obj3 = json.dumps({u"result": 'OK'})
	json_obj4 = json.dumps({u"result": 'failed, user has already logined'})

	cnxn = connect()
	cursor = cnxn.cursor()
	
	if cursor.execute("select UserPSW, UserLogin from [dbo].[Users] where UserEmail = ?", User_Email).rowcount == 0:
		return json_obj1
	else:
		list = cursor.fetchone()
		userpsw = list[0]
		userlogin = list[1]
		if userpsw != User_PSW:
			return json_obj2
		else: 
			if userlogin == 1:
				return json_obj4
			else:
				return json_obj3
	cnxn.close()

# post user logout status to DB when user logout
# so that one user account cannot login on two devices at the same time
@hug.post('/logout')
def logout(User_Email: hug.types.text):
	cnxn = connect()
	cursor = cnxn.cursor()
	cursor.execute("update [dbo].[Users] set UserLogin = ? where UserEmail = ?", 0, User_Email)
	cnxn.commit()
	json_obj = json.dumps({u"result":'OK'})
	return json_obj
	cnxn.close()

# get user information, such as username, image, contact info..., by user email
@hug.get('/getUser')
def getUser(User_Email:hug.types.text):
	cnxn = connect()
	cursor = cnxn.cursor()
	cursor.execute("select * from [dbo].[Users] where UserEmail = ?", User_Email)
	list = cursor.fetchone()
	UserName = list[0]
	# UserIMG = list[2]
	UserMobile = list[3]
	# UserEmail = list[4]
	json_obj = json.dumps({"result": "OK","username": UserName, "usermobile":UserMobile})
	return json_obj
	cnxn.close()

# get creature information, such as creature name, dangerous level, habitat info..., 
# by creature id
@hug.get('/getCreature')
def getCreature(Creature_ID: hug.types.text):
	cnxn = connect()
	cursor = cnxn.cursor()
	cursor.execute("select * from [dbo].[Creature] where CreatureID = ?", Creature_ID)
	list = cursor.fetchone()
	CreatureName = list[1]
	CreatureDescription = list[4]
	Species = list[3]
	# CreatureIMG = list[2]
	DangerLevel = list[5]
	DangerDescription = list[6]
	HabitCategory = list[7]
	HabitDescription = list[8]
	# HabitIMG = list[9]
	json_obj = json.dumps({u"result":'OK',
				"creaturename":CreatureName,
				"creaturedescription":CreatureDescription,
				# "creatureimg":CreatureIMG,
				"species": Species,
				"dangerlevel":DangerLevel,
				"dangerdescription":DangerDescription,
				"habitcategory":HabitCategory,
				"habitdescription":HabitDescription,
				# "habitimg":HabitIMG'
				})
	return json_obj
	cnxn.close()

# user report thier encounter to the server
@hug.post('/report')
def report(User_Email: hug.types.text, User_LocationX: hug.types.text, User_LocationY: hug.types.text, User_Description: hug.types.text, Input_Specises: hug.types.text, Create_Date: hug.types.text):
	UserX = round(Decimal(User_LocationX),4)
	UserY = round(Decimal(User_LocationY),4)
	cnxn = connect()
	cursor = cnxn.cursor()
	cursor.execute("select X, Y from [dbo].[Report] where UserEmail = ? and X = ? and Y = ? and Species = ? and Date = ?", User_Email, UserX, UserY, Input_Specises, Create_Date)
	if cursor.rowcount == 0:
		with cursor.execute("insert into [dbo].[Report] values (?, ?, ?, ?, ?, ?)", (User_Email, UserX, UserY, User_Description, Input_Specises, Create_Date)):
			json_obj = json.dumps({u"result":'OK'})
			# print(json_obj)
			return json_obj
	else: 
		json_obj1 = json.dumps({u"result": 'you have reported something similar'})
		# print(json_obj1)
		return json_obj1
	cnxn.close()

# user can get nearby reports locations and they were displayed (marker) on google map
@hug.get('/getReportLocation')
def getReportLocations(User_LocationX: hug.types.text, User_LocationY: hug.types.text):
	cnxn = connect()
	cursor = cnxn.cursor()
	UserX_max =	Decimal(User_LocationX) + Decimal(0.01)
	UserX_min = Decimal(User_LocationX) - Decimal(0.01)
	UserY_max = Decimal(User_LocationY) + Decimal(0.01)
	UserY_min = Decimal(User_LocationY) - Decimal(0.01)
	if cursor.execute("select X, Y from [dbo].[Report] where X BETWEEN ? AND ? and Y BETWEEN ? AND ?",UserX_min,UserX_max,UserY_min,UserY_max).rowcount == 0:
		json_obj = json.dumps({u"result":'safe'})
		# print (json_obj)
		return json_obj
	else:
		list = cursor.fetchall()
		rowarray_list = []
		for row in list:
			t = (str(row.X),str(row.Y))
			rowarray_list.append(t)
		json_obj1 = json.dumps({u"result":'ReportList',
					"locations":rowarray_list})
		# print (json_obj1)
		return json_obj1
	cnxn.close()

# user can read each report content, such as who reported it, when reported it...,
# by send the server a location address of that report marker
@hug.get('/getReport')
def getReport(LocationX: hug.types.text, LocationY: hug.types.text):
	rowarray_list = []
	cnxn = connect()
	cursor = cnxn.cursor()
	cursor.execute("select UserEmail, Date, Species, ReportContent from [dbo].[Report] where X = ? AND Y = ? order by Date desc", LocationX, LocationY)
	list = cursor.fetchall()
	for row in list:
		cursor.execute("select UserName from [dbo].[Users] where UserEmail = ?", row.UserEmail)
		list1 = cursor.fetchone()
		username = list1[0]
		t = (username, str(row.Date), row.Species, row.ReportContent)
		rowarray_list.append(t)
	json_obj = json.dumps({u"result":'OK',"reports":rowarray_list})
	# print(json_obj)
	return json_obj
	cnxn.close()

# user can get nearby creatures locations and they were displayed (marker) on google map
@hug.get('/getCreatureList')
def getCreatureList(User_LocationX: hug.types.text, User_LocationY: hug.types.text):
	cnxn = connect()
	cursor = cnxn.cursor()
	UserX_max =	Decimal(User_LocationX) + Decimal(0.02)
	UserX_min = Decimal(User_LocationX) - Decimal(0.02)
	UserY_max = Decimal(User_LocationY) + Decimal(0.02)
	UserY_min = Decimal(User_LocationY) - Decimal(0.02)
	if cursor.execute("select * from [dbo].[Location] a right join [dbo].[CreatureList] b on a.LocationID = b.LocationID right join [dbo].[Creature] c on c.CreatureID = b.CreatureID where a.LocationX BETWEEN ? AND ? and a.LocationY BETWEEN ? AND ?",UserX_min,UserX_max,UserY_min,UserY_max).rowcount == 0:
		json_obj = json.dumps({u"result":'safe'})
		# print (json_obj)
		return json_obj
	else:
		list = cursor.fetchall()
		rowarray_list = []
		for row in list:
			t = (str(row.LocationX),str(row.LocationY),row.Species,row.CreatureID)
			rowarray_list.append(t)
		json_obj1 = json.dumps({u"result":'CreatureList',
					"locations":rowarray_list})
		# print (json_obj1)
		return json_obj1
	cnxn.close()

# user can get nearby safe area locations by sending the user current location to server
@hug.get('/getSafeArea')
def getSafeArea(User_LocationX: hug.types.text, User_LocationY: hug.types.text):
	rowarray_list = []
	cnxn = connect()
	cursor = cnxn.cursor()
	UserX_max =	Decimal(User_LocationX) + Decimal(0.003)
	UserX_min = Decimal(User_LocationX) - Decimal(0.003)
	UserY_max = Decimal(User_LocationY) + Decimal(0.003)
	UserY_min = Decimal(User_LocationY) - Decimal(0.003)
	if cursor.execute("select * from [dbo].[Location] where LocationX BETWEEN ? AND ? and LocationY BETWEEN ? AND ?",UserX_min,UserX_max,UserY_min,UserY_max).rowcount == 0:
		json_obj = json.dumps({u"result":'you are in a safe area'})
		print (json_obj)
		return json_obj
	else:
		UserX_new1 = Decimal(User_LocationX) + Decimal(0.003)
		UserY_new1 = Decimal(User_LocationY) + Decimal(0.003)
		UserX_new1_min = UserX_new1 - Decimal(0.002)
		UserX_new1_max = UserX_new1 + Decimal(0.002)
		UserY_new1_min = UserY_new1 - Decimal(0.002)
		UserY_new1_max = UserY_new1 + Decimal(0.002)

		UserX_new2 = Decimal(User_LocationX) + Decimal(0.003)
		UserY_new2 = Decimal(User_LocationY) - Decimal(0.003)
		UserX_new2_min = UserX_new2 - Decimal(0.002)
		UserX_new2_max = UserX_new2 + Decimal(0.002)
		UserY_new2_min = UserY_new2 - Decimal(0.002)
		UserY_new2_max = UserY_new2 + Decimal(0.002)

		UserX_new3 = Decimal(User_LocationX) - Decimal(0.003)
		UserY_new3 = Decimal(User_LocationY) + Decimal(0.003)
		UserX_new3_min = UserX_new3 - Decimal(0.002)
		UserX_new3_max = UserX_new3 + Decimal(0.002)
		UserY_new3_min = UserY_new3 - Decimal(0.002)
		UserY_new3_max = UserY_new3 + Decimal(0.002)

		UserX_new4 = Decimal(User_LocationX) - Decimal(0.003)
		UserY_new4 = Decimal(User_LocationY) - Decimal(0.003)
		UserX_new4_min = UserX_new4 - Decimal(0.002)
		UserX_new4_max = UserX_new4 + Decimal(0.002)
		UserY_new4_min = UserY_new4 - Decimal(0.002)
		UserY_new4_max = UserY_new4 + Decimal(0.002)

		UserX_new5 = Decimal(User_LocationX) + Decimal(0.003)
		UserY_new5 = Decimal(User_LocationY) 
		UserX_new5_min = UserX_new5 - Decimal(0.002)
		UserX_new5_max = UserX_new5 + Decimal(0.002)
		UserY_new5_min = UserY_new5 - Decimal(0.002)
		UserY_new5_max = UserY_new5 + Decimal(0.002)

		UserX_new6 = Decimal(User_LocationX) - Decimal(0.003)
		UserY_new6 = Decimal(User_LocationY) 
		UserX_new6_min = UserX_new6 - Decimal(0.002)
		UserX_new6_max = UserX_new6 + Decimal(0.002)
		UserY_new6_min = UserY_new6 - Decimal(0.002)
		UserY_new6_max = UserY_new6 + Decimal(0.002)

		UserX_new7 = Decimal(User_LocationX)
		UserY_new7 = Decimal(User_LocationY) - Decimal(0.003)
		UserX_new7_min = UserX_new7 - Decimal(0.002)
		UserX_new7_max = UserX_new7 + Decimal(0.002)
		UserY_new7_min = UserY_new7 - Decimal(0.002)
		UserY_new7_max = UserY_new7 + Decimal(0.002)

		UserX_new8 = Decimal(User_LocationX) 
		UserY_new8 = Decimal(User_LocationY) + Decimal(0.003)
		UserX_new8_min = UserX_new8 - Decimal(0.002)
		UserX_new8_max = UserX_new8 + Decimal(0.002)
		UserY_new8_min = UserY_new8 - Decimal(0.002)
		UserY_new8_max = UserY_new8 + Decimal(0.002)

		with cursor.execute("select * from [dbo].[Location] where LocationX BETWEEN ? AND ? and LocationY BETWEEN ? AND ?",UserX_new1_min,UserX_new1_max,UserY_new1_min,UserY_new1_max):
			if cursor.rowcount==0:
				t=(str(UserX_new1),str(UserY_new1))
				rowarray_list.append(t)

		with cursor.execute("select * from [dbo].[Location] where LocationX BETWEEN ? AND ? and LocationY BETWEEN ? AND ?",UserX_new2_min,UserX_new2_max,UserY_new2_min,UserY_new2_max):
			if cursor.rowcount==0:
				t=(str(UserX_new2),str(UserY_new2))
				rowarray_list.append(t)

		with cursor.execute("select * from [dbo].[Location] where LocationX BETWEEN ? AND ? and LocationY BETWEEN ? AND ?",UserX_new3_min,UserX_new3_max,UserY_new3_min,UserY_new3_max):
			if cursor.rowcount==0:
				t=(str(UserX_new3),str(UserY_new3))
				rowarray_list.append(t)
		
		with cursor.execute("select * from [dbo].[Location] where LocationX BETWEEN ? AND ? and LocationY BETWEEN ? AND ?",UserX_new4_min,UserX_new4_max,UserY_new4_min,UserY_new4_max):
			if cursor.rowcount==0:
				t=(str(UserX_new4),str(UserY_new4))
				rowarray_list.append(t)

		with cursor.execute("select * from [dbo].[Location] where LocationX BETWEEN ? AND ? and LocationY BETWEEN ? AND ?",UserX_new5_min,UserX_new5_max,UserY_new5_min,UserY_new5_max):
			if cursor.rowcount==0:
				t=(str(UserX_new5),str(UserY_new5))
				rowarray_list.append(t)

		with cursor.execute("select * from [dbo].[Location] where LocationX BETWEEN ? AND ? and LocationY BETWEEN ? AND ?",UserX_new6_min,UserX_new6_max,UserY_new6_min,UserY_new6_max):
			if cursor.rowcount==0:
				t=(str(UserX_new6),str(UserY_new6))
				rowarray_list.append(t)

		with cursor.execute("select * from [dbo].[Location] where LocationX BETWEEN ? AND ? and LocationY BETWEEN ? AND ?",UserX_new7_min,UserX_new7_max,UserY_new7_min,UserY_new7_max):
			if cursor.rowcount==0:
				t=(str(UserX_new7),str(UserY_new7))
				rowarray_list.append(t)

		with cursor.execute("select * from [dbo].[Location] where LocationX BETWEEN ? AND ? and LocationY BETWEEN ? AND ?",UserX_new8_min,UserX_new8_max,UserY_new8_min,UserY_new8_max):
			if cursor.rowcount==0:
				t=(str(UserX_new8),str(UserY_new8))
				rowarray_list.append(t)

		json_obj1 = json.dumps({u"result":'SafeList',
					"locations":rowarray_list})
		print(json_obj1)
		return json_obj1
	cnxn.close()

# if __name__ == '__main__':
		#register('ching', '123456', '0478171327', 'ching@gmail.com')
		# logout('ching33333@gmail.com')
		# getUser('ching33333@gmail.com')
		# getReportLocations('-37.7452358','144.9637866')
		# getReport('-37.7975', '144.9666')
		# getCreatureList('-37.7975', '144.9666')
		# report('karen@gmail.com', '-37.7000', '144.9665837', 'i met a snake here', 'snake', '2017-10-03')
		# getSafeArea('-37.797495','144.966565')
		