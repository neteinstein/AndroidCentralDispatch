# Android Central Dispatch

A minimalistic Dispatch Center for Android: Define the ammount of threads each module may run at each time, and dispatch the runnable for the central ThreadPool


General Usage and API
---------------------
1. In Android Central Dispatch, you need to first create an instance of the Dispatcher to define the total ammount of threads in use.

<code>AndroidDispatch androidDispatch = AndroidDispatch.getInstance(int maxPoolSize)</code>

2. Then every module (class) that wants to run tasks needs to register to define the ammount of threads it may take.
<code>androidDispatch.register(int numberOfMaxThreadsForThisModule)</code>

3. To submit tasks (runnables) you need to instanciate a DispatchRunnable instead of a Runnable (don't worry the name is the only thing different)

<code>DispatchRunnable r = new DispatchRunnable... <code>

4. Submit your tasks 
<code>androidDispatch.submitTask(DispatchRunnable r)</code>  
(the class where you call the method NEEDS to be the same which subscribed)

or 

<code>androidDispatch.submitTask(Class clazz, DispatchRunnable r)</code>  
(the class where you call the method DOES NOT NEED to be the same which subscribed)


5. That all folks, just don't forget to unregister when you don't need it anymore, and destroy before closing the app.
<code>androidDispatch.unregister()</code>

and 

<code>androidDispatch.destroy(boolean mayInterruptThreads)</code>


### V0.0.1 (2015-02-29): First draft code

License
-------
Copyright (C) 2012-2014 Pedro Vicente, neteinstein.org

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
