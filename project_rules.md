# Project Coding Standard
> Based strictly on course lecture materials by **Tal Zion**.
> Do NOT use patterns, libraries, or approaches not listed here without consulting the lecturer first.

### 📋 Indexed Lectures
| # | Title | Status |
|---|---|---|
| 1 | Kotlin Introduction | ✅ Indexed |
| 3 | Introduction to Android | ✅ Indexed |
| 5 (Part 1a) | Views and Layouts | ✅ Indexed |
| 5 (Part 1b) | Lists in Android (ListView) | ✅ Indexed |
| 5 (Part 2) | RecyclerView | ✅ Indexed |
| - | Room: SQLite Persistence | ✅ Indexed |
| - | Google App Architecture (MVVM + LiveData) | ✅ Indexed |
| - | Local and Remote Database Synchronization | ✅ Indexed |
| - | Online Persistency with Firebase Cloud Firestore | ✅ Indexed |
| - | Capturing Images in Android Fragments | ✅ Indexed |
| - | GeoLocation in Modern Applications | ✅ Indexed |
| - | Retrofit | ✅ Indexed |
| - | Android Fragments | ✅ Indexed |
| - | Navigation in Android Development | ✅ Indexed |
| - | Final Project Guidelines | ✅ Indexed |

---

## 1. Architecture

### Pattern: **MVVM (Model-View-ViewModel)**
The teacher explicitly uses the **Google App Architecture / MVVM** pattern with the following layers:

| Layer | Responsibility | Class Type |
|---|---|---|
| **View** | Displays data, handles user events | `Fragment` / `Activity` |
| **ViewModel** | Holds UI-related data, survives config changes, no View references | `ViewModel` subclass |
| **Model** | Fetches/stores data (Firebase + Room), exposes `LiveData` | Plain class / Singleton |

### Key Architecture Rules
- **Never store app data or state directly in Activities or Fragments.**
- **Components must not depend on each other directly** — communicate through ViewModel and LiveData.
- **Separation of Concerns:** Keep business logic out of Activities/Fragments.
- **Drive UI from Model:** Models handle data independently from Views and lifecycle.
- The `Model` is a **singleton** (`object` or `companion object`) — single source of truth.
- The `ViewModel` acts as a bridge between Model and UI — it exposes `LiveData`, never raw data.

### Architecture Flow
```
Fragment  →  observes  →  ViewModel  →  exposes LiveData from  →  Model  →  Firebase / Room
```

---

## 2. Kotlin Language Features (Lecture 1)

### Enums
```kotlin
// Simple enum
enum class Direction { NORTH, SOUTH, EAST, WEST }

// Enum with properties and methods
enum class Status(val code: Int) {
    ACTIVE(1), INACTIVE(0), PENDING(2);
    fun isFinal(): Boolean = this == ACTIVE || this == INACTIVE
}
```

### Sealed Classes
- Subclasses must be in the **same file** as the sealed class
- Enables **exhaustive `when`** — no `else` branch needed if all subclasses are covered
- Implicitly abstract — cannot be instantiated directly
```kotlin
sealed class Result {
    data class Success(val data: String) : Result()
    data class Error(val exception: Exception) : Result()
}

fun handleResult(result: Result) {
    when (result) {
        is Result.Success -> println("Success: ${result.data}")
        is Result.Error   -> println("Error: ${result.exception.message}")
    }
}
```

### For Loops
```kotlin
for (i in 1..3) { }                          // 1, 2, 3
for (i in 6 downTo 0 step 2) { }            // 6, 4, 2, 0
for (fruit in listOf("Apple", "Banana")) { } // collection iteration
for ((index, color) in colors.withIndex()) { } // with index
for (index in colors.indices) { }            // index only
```

### Arrays
```kotlin
val numbers: Array<Int> = arrayOf(1, 2, 3)          // boxed
val intArray: IntArray = intArrayOf(10, 20, 30)      // primitive (preferred for performance)
val squares = Array(5) { i -> i * i }                // lambda initializer
fruitArray[1] = "Blueberry"                          // modify by index
fruitArray.get(0)                                    // access via get()
```
- Array **size is immutable** after creation; contents can be mutated
- Use `IntArray`, `CharArray`, `BooleanArray` etc. to avoid boxing overhead

### Extension Functions
- Add functions to existing classes without inheritance
- Resolved **statically at compile time** (not virtual/polymorphic)
```kotlin
fun MutableList<Int>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}
```

### Object Expressions (Anonymous Objects)
```kotlin
val point = object {
    val x = 10
    val y = 20
}
```
- Use for one-time-use objects or interface implementations
- Instances are called **anonymous objects**

### Singleton Pattern (Companion Object)
```kotlin
class DatabaseManager private constructor() {
    companion object {
        @Volatile private var instance: DatabaseManager? = null
        fun getInstance(): DatabaseManager =
            instance ?: synchronized(this) {
                instance ?: DatabaseManager().also { instance = it }
            }
    }
}
```
- `companion object` = class-level, created once, shared across all instances
- `private constructor` prevents external instantiation

### Inheritance Rules
- All classes are `final` by default — use `open` to allow subclassing
- Abstract classes: implicitly open, cannot be instantiated
- Common superclass for all: `Any` (provides `equals()`, `hashCode()`, `toString()`)
- Base class must be initialized in derived class primary constructor

### Properties with Custom Accessors
```kotlin
var name: String = ""
    get() = field.uppercase()
    set(value) {
        field = value.trim()  // 'field' = backing field, prevents infinite recursion
    }
```
- `field` identifier only valid inside property accessors
- Kotlin provides backing field automatically when needed

---

### Data Classes
```kotlin
data class Student(
    val id: String,
    val name: String,
    val grade: Int
)
```
- Automatically generates: `equals()`, `hashCode()`, `toString()`, `copy()`
- `copy()` creates a modified copy: `val updated = student.copy(grade = 95)`
- Use for model/entity objects — **preferred over regular classes for data holders**
- Must have at least one parameter in the primary constructor

### `when` Expression
```kotlin
// As statement
when (x) {
    1 -> println("one")
    2, 3 -> println("two or three")
    in 4..10 -> println("between 4 and 10")
    is String -> println("is a string")
    else -> println("other")
}

// As expression (returns a value)
val result = when (direction) {
    Direction.NORTH -> "Going north"
    Direction.SOUTH -> "Going south"
    else -> "Other direction"
}
```
- `else` is required when `when` is used as an **expression** (unless all cases are covered, e.g. sealed class)
- `when` with **no argument** replaces `if-else if` chains

### String Templates
```kotlin
val name = "Rotem"
val age = 25
println("Name: $name, Age: $age")           // simple variable
println("Name length: ${name.length}")       // expression in braces
println("Is adult: ${age >= 18}")            // any expression
```

### Lambdas & Higher-Order Functions
```kotlin
// Lambda syntax
val sum: (Int, Int) -> Int = { a, b -> a + b }
val greet: (String) -> Unit = { name -> println("Hello, $name") }

// Trailing lambda (last parameter)
button.setOnClickListener { view ->
    // handle click
}

// Higher-order function — takes function as parameter
fun doOperation(x: Int, y: Int, operation: (Int, Int) -> Int): Int {
    return operation(x, y)
}
val result = doOperation(3, 4) { a, b -> a + b }   // result = 7
```

### Collections — List, MutableList, Map
```kotlin
// Immutable list — read only
val fruits: List<String> = listOf("Apple", "Banana", "Cherry")

// Mutable list — can add/remove
val students: MutableList<String> = mutableListOf("Alice", "Bob")
students.add("Charlie")
students.remove("Bob")
students[0] = "Alicia"   // update by index

// Common operations
fruits.size                         // count
fruits.isEmpty()                    // check empty
fruits.contains("Apple")            // membership
fruits.filter { it.length > 5 }     // filter — returns new list
fruits.map { it.uppercase() }       // transform — returns new list
fruits.forEach { println(it) }      // iterate
fruits.first()                      // first element (throws if empty)
fruits.firstOrNull()                // first or null

// HashMap
val map = HashMap<String, Int>()
map["Alice"] = 90
map["Bob"] = 85
val grade = map["Alice"]            // returns Int? (nullable)
val safe = map.getOrDefault("Eve", 0)

// Immutable map
val scores = mapOf("Alice" to 90, "Bob" to 85)
```

---

## 2b. Android Application Fundamentals (Lecture 3)

### Application Package Structure
- Android apps are written in **Java or Kotlin**
- The `aapt` tool compiles code together with data and resource files
- Everything is bundled into a single **`.apk`** (Android Package) archive
- All code in one `.apk` file = one complete application

---

### Android Security Model
- Every app runs in its own dedicated **Linux process**
- Android starts the process when code needs execution and shuts it down when resources are needed
- Each app gets a unique **Linux user ID** at install time
- App files are only accessible to that user ID by default
- Apps can share data only through explicit permissions

---

### Component-Based Architecture
Android apps have **no single entry point** (no `main()` function). Instead, the system instantiates individual components as needed.

One application can use components from other applications seamlessly.

---

### The Four Application Components

| Component | Purpose |
|---|---|
| **Activity** | Visual UI screen the user interacts with directly |
| **Service** | Background operation with no user interface |
| **Broadcast Receiver** | Responds to system-wide or app-specific announcements |
| **Content Provider** | Manages and shares structured app data with other apps |

---

### Activity Component
- Presents a visual user interface to the user
- Each activity operates **independently** of others
- Implemented as a subclass of the `Activity` base class
- One activity is designated as the **launch point** when the app starts
- Moving between activities = current activity starts the next one

---

### Service Component
- Runs continuously **without a UI**, extending the `Service` base class
- Apps can **bind** to running services and communicate through exposed interfaces
- Services run on the **main thread** — must spawn additional threads for time-consuming tasks
> ⚠️ Services run on the main thread like activities. Spawn separate threads for intensive operations to avoid blocking the app.

---

### Broadcast Receiver Component
- Responds to system-wide or app-specific broadcast announcements
- Operates **without showing any UI**
- Can start activities in response to received information
- Can alert users via `NotificationManager` (backlight, vibration, sounds, status bar)

**System broadcasts include:**
- Battery low warnings
- Picture captured notifications
- Language preference changes
- Network connectivity changes
- Screen on/off events

**Application broadcasts include:**
- Data download completion
- Custom app events
- Inter-app communication
- Background task completion

---

### Content Provider Component
- Manages access to structured sets of application data
- Makes specific app data available to **other applications**
- Data can be stored in: file system, **SQLite databases**, or any custom storage
- Other apps access data through a **`ContentResolver`** object
- `ContentResolver` communicates with any content provider consistently regardless of underlying storage

---

### Activating Components

| Component | How It's Activated |
|---|---|
| **Content Provider** | Targeted by a `ContentResolver` request |
| **Activity / Service / Broadcast Receiver** | Activated by **Intents** (asynchronous messages) |

**Intents** are the primary mechanism for component activation — enabling loose coupling between components and applications.

---

### Understanding Intents

**For Activities & Services:**
- Specify the **action** being requested
- Identify the **data** to act upon via URI
- Example actions: viewing, editing, sharing content

**For Broadcast Receivers:**
- Announce the **action that has occurred**
- Enable publish-subscribe pattern for system-wide event notification
- Example events: connectivity changes, battery status, custom app events

---

### The Manifest File (`AndroidManifest.xml`)
The manifest declares all essential information about the app to the Android system. Without it, the system cannot run any app components.

**Three responsibilities:**
| Responsibility | Description |
|---|---|
| **Component Declaration** | Every Activity, Service, Broadcast Receiver, and Content Provider **must** be registered here |
| **Library Dependencies** | Names any external libraries the app requires |
| **Permission Requirements** | Identifies permissions needed to access protected system features or data |

The manifest is bundled into the `.apk` and serves as the primary descriptor of app structure and requirements.

**Typical manifest structure:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service android:name=".MyService" />

        <receiver android:name=".MyReceiver" />

    </application>

</manifest>
```

---

### Activating Activities — Intent Patterns

```kotlin
// 1. Start a new activity (one-way)
val intent = Intent(this, TargetActivity::class.java)
startActivity(intent)

// 2. Start activity and get a result back
startActivityForResult(intent, REQUEST_CODE)

// 3. Launched activity reads its intent
val data = getIntent()

// 4. Running activity receives new intents
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    // handle updated intent
}
```

**Stopping Activities:**
```kotlin
// Self-terminate
finish()

// Terminate another activity
finishActivity(requestCode)
```
> Any data that needs to persist should be saved **before** calling `finish()`.

---

### Activity Communication Flow (startActivityForResult)

1. **Intent Creation** — First activity creates an `Intent` with target + optional data
2. **Activity Launch** — System starts the target using `startActivityForResult()`
3. **Processing** — Target activity performs its task and prepares result data
4. **Result Return** — Target calls `setResult()` then `finish()`
5. **Result Handling** — First activity receives result in `onActivityResult()` callback

```kotlin
// Sending result from target activity
val resultIntent = Intent()
resultIntent.putExtra("result_key", "result_value")
setResult(Activity.RESULT_OK, resultIntent)
finish()

// Receiving result in calling activity
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
        val result = data?.getStringExtra("result_key")
    }
}
```

---

### The Activity Stack (Back Stack / Task)

- A **Task** = a group of related activities arranged in a stack
- The **bottom activity** = root activity that started the task
- The **top activity** = the currently running activity the user sees
- Pressing **BACK** pops the current activity, reveals and resumes the previous one

**Stack rules:**
- Activities are **never rearranged** — only pushed or popped
- The stack stores **actual activity objects** (not references) — each with its own state
- The same `Activity` subclass can appear **multiple times** in the stack as separate instances

---

### Clearing the Activity Stack

Controlled via attributes in `AndroidManifest.xml` on the `<activity>` element:

| Attribute | Behavior |
|---|---|
| *(default)* | After long absence, system clears all activities except the root |
| `alwaysRetainTaskState="true"` | Task retains **all** activities indefinitely — use when full history is important |
| `clearTaskOnLaunch="true"` | Stack cleared to root every time user leaves and returns |
| `finishOnTaskLaunch="true"` | Like `clearTaskOnLaunch` but operates on a **single activity** — can apply to root too |

```xml
<activity
    android:name=".MainActivity"
    android:alwaysRetainTaskState="true" />
```

---

### Starting Tasks — Launcher Intent Filter

An activity becomes the **task entry point** by declaring this intent filter in the manifest:

```xml
<activity android:name=".MainActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />       <!-- primary entry point -->
        <category android:name="android.intent.category.LAUNCHER" /> <!-- appears in app launcher -->
    </intent-filter>
</activity>
```

- Causes an **icon and label** to appear in the system app launcher
- Gives users a way to launch the task initially AND return to it at any time

---

### Processes and Threads

- When the first component starts, Android creates a new **Linux process** with a single thread
- That single thread = the **main thread** (also called the **UI thread**)
- By default, all components of the same app run in the **same process and thread**
- Additional components can run in other processes if configured in the manifest
- You can spawn additional threads within any process

**Critical rule — Never block the main thread:**
> Blocking the main thread blocks ALL components in the process → app freezes → **ANR (Application Not Responding)** dialog.

| ✅ Main thread | ❌ NOT on main thread |
|---|---|
| UI updates | Network calls |
| Lifecycle callbacks | Database operations |
| User event handling | File I/O |
| | Long computations |

---

### Process Lifecycle Management

- Android may **terminate a process at any point** when system resources are needed
- All components running in that process are destroyed when the process is killed
- When components are needed again, Android **automatically restarts** the process
- Proper **state preservation** is essential because of this transparent restart mechanism

---

### Thread Management

```kotlin
// Standard Java thread
Thread {
    // background work here
    MyApplication.Globals.mainHandler.post {
        // return to main thread for UI updates
    }
}.start()
```

**Rules:**
1. Threads perform time-consuming operations without blocking the main UI thread
2. The thread hosting an activity should **never** perform long-running operations
3. Anything that may not complete quickly **must** be assigned to a different thread
4. This includes: network calls, database operations, file I/O, long computations

> See Section 7.3 for the project's standard threading pattern using `ExecutorService` + `Handler`.

---

### Activity Lifecycle States

| State | Description |
|---|---|
| **Active** | In the foreground, at top of stack — visible and interactive |
| **Paused** | Lost focus but still visible (e.g. a dialog is on top) — alive but may be killed in low memory |
| **Stopped** | Completely obscured by another activity — not visible, retains state, prime candidate for termination |

---

### Activity Lifecycle Callbacks

```
onCreate → onStart → onResume → [running] → onPause → onStop → onDestroy
                                                  ↑                ↓
                                             onRestart ←←←←←←←←←←
```

| Callback | When Called | Typical Use |
|---|---|---|
| `onCreate(Bundle)` | Activity first created | Initialize essential components, call `setContentView()` |
| `onStart()` | Activity becomes visible | Register broadcast receivers, bind to services |
| `onResume()` | Activity starts interacting with user | Start animations, acquire exclusive resources (camera) |
| `onPause()` | System about to resume another activity | Pause operations, save persistent state, release exclusive resources — **must be fast** |
| `onStop()` | Activity no longer visible | Unregister receivers, unbind services, release invisible resources |
| `onRestart()` | After stopped, prior to starting again | Restore stopped state |
| `onDestroy()` | Before activity is destroyed | Final cleanup of all resources |

> When an activity is paused or stopped, the system can drop it from memory. When displayed again, it must be **completely restarted and restored** to its previous state.

---

### Three Nested Lifecycle Loops

| Loop | Spans | Purpose |
|---|---|---|
| **Entire lifetime** | `onCreate` → `onDestroy` | Full resource management |
| **Visible lifetime** | `onStart` → `onStop` | Resources needed while visible |
| **Foreground lifetime** | `onResume` → `onPause` | Resources needed while interactive |

**Visible lifetime (`onStart` / `onStop`):**
- `onStart()`: register broadcast receivers, bind to services, begin observing data affecting UI
- `onStop()`: unregister receivers, unbind services, stop observing data
- Activities can go through visible lifetime **multiple times** as users navigate

**Foreground lifetime (`onResume` / `onPause`):**
- `onResume()`: start animations, begin audio, acquire exclusive resources (camera)
- `onPause()`: pause operations, save persistent state, release exclusive resources
- ⚠️ `onPause()` **must execute quickly** — next activity cannot resume until it returns
- Foreground lifetime can be **very brief** — design for frequent resume/pause cycles

---

### Saving Persistent State

Two distinct types of persistent state:

| Type | Storage | API |
|---|---|---|
| **Shared / Document-like data** | SQLite database | Room (see Section 7.1) |
| **Internal state & preferences** | Key-value store | `SharedPreferences` |

**SharedPreferences — Reading:**
```kotlin
val prefs = getSharedPreferences(TAG, MODE_PRIVATE)
val lastActivity = prefs.getString("parameter_1", "default_value")
```

**SharedPreferences — Writing:**
```kotlin
val editor = prefs.edit()
editor.putString("parameter_1", "my_value")
editor.apply()   // async (preferred) — or commit() for synchronous save
```

**Scope:**
```kotlin
getPreferences(MODE_PRIVATE)          // preferences scoped to this Activity only
getSharedPreferences(TAG, MODE_PRIVATE) // preferences shared across multiple components
```

> ⚠️ Always call `commit()` or `apply()` after editing. Changes are **not persisted** until one of these is called.
> Use `apply()` for async background save (preferred). Use `commit()` only when you need a synchronous guarantee.

---

## 3. UI Approach

### **XML Layouts only**
- All UI is declared in **XML layout files** under `res/layout/`.
- No Jetpack Compose is used.
- **ViewBinding** is mentioned as an option (`binding?.view`) but `findViewById<T>()` is the primary pattern shown in slides.
- UI is attached in `Activity.onCreate()` via:
  ```kotlin
  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.main_layout)
  }
  ```

### Layout Managers (use in this order of preference)
1. `ConstraintLayout` — **preferred**, flat hierarchy, best performance
2. `LinearLayout` — simple sequential arrangements
3. `TableLayout` — tabular data (rows/columns via `TableRow`)
4. `RelativeLayout` — relative positioning
5. `FrameLayout` — single-view-at-a-time / fragment containers
6. ❌ `AbsoluteLayout` — **deprecated, do not use**

### Dimension Units
- Use **`dp`** for all layout dimensions
- Use **`sp`** for all text sizes
- ❌ Never use `px` for layouts

### Resource Organization
```
res/layout/      → XML layout files
res/values/      → strings, colors, dimensions, styles
res/drawable/    → images and graphics
res/menu/        → menu definitions
```

---

## 4. Widgets & Views (Lecture 5 Part 1)

### Text Views
```kotlin
val tv = findViewById<TextView>(R.id.myTextView)
// Auto-link URLs, emails, phone numbers:
Linkify.addLinks(tv, Linkify.ALL)
// Options: Linkify.WEB_URLS, Linkify.EMAIL_ADDRESSES, Linkify.PHONE_NUMBERS, Linkify.ALL
```

### EditText Key Attributes (XML)
```xml
android:capitalize="words"       <!-- auto-capitalize -->
android:phoneNumber="true"       <!-- restrict to phone format -->
android:password="true"          <!-- mask input -->
android:singleLine="true"        <!-- prevent line breaks -->
```

### AutoCompleteTextView with ArrayAdapter
```kotlin
val actv = findViewById<AutoCompleteTextView>(R.id.myActv)
val aa = ArrayAdapter(
    this,
    android.R.layout.simple_dropdown_item_1line,
    arrayOf("English", "Hebrew", "Hindi", "Spanish", "German", "Greek")
)
actv.setAdapter(aa)
```

### Button Styling (XML)
```xml
<Button
    android:id="@+id/ccbtn1"
    android:text="@string/basicBtnLabel"
    android:typeface="serif"
    android:textStyle="bold"
    android:layout_width="fill_parent"
    android:layout_height="wrap_content" />
```

### ImageButton
```xml
<ImageButton
    android:id="@+id/imageBtn"
    android:src="@drawable/btnImage"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```
```kotlin
val btn = findViewById<ImageButton>(R.id.imageBtn)
btn.setImageResource(R.drawable.icon)
```

### ToggleButton
```xml
<ToggleButton
    android:id="@+id/cctglBtn"
    android:textOn="Run"
    android:textOff="Stop"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

### CheckBox Key Methods
```kotlin
checkBox.setChecked(true)
checkBox.toggle()
val state = checkBox.isChecked
checkBox.setOnCheckedChangeListener { _, isChecked -> }
```

### RadioButton inside RadioGroup
```xml
<RadioGroup
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">
    <RadioButton android:id="@+id/chRBtn"   android:text="Chicken" />
    <RadioButton android:id="@+id/beefRBtn" android:text="Beef" />
</RadioGroup>
```
- Only one `RadioButton` in a `RadioGroup` can be selected at a time

### gravity vs layout_gravity
| Attribute | Affects | Example |
|---|---|---|
| `android:gravity` | Content **inside** the view | Centers text inside a `TextView` |
| `android:layout_gravity` | The **view itself** within its parent | Centers a `Button` inside its parent |

### View Visibility States
```kotlin
view.visibility = View.VISIBLE    // shown
view.visibility = View.INVISIBLE  // hidden, still takes space
view.visibility = View.GONE       // hidden, takes no space
```

---

### ListView (Lecture 5 Part 1b)

**What is ListView?**
- Displays items in a vertically scrolling list
- Activity typically extends `android.app.ListActivity`
- `setListAdapter()` / `setAdapter()` binds the data source to the ListView
- All adapter-based views extend `android.widget.AdapterView`

**Adapter-based Views**
| Widget | Purpose |
|---|---|
| `ListView` | Vertical scrolling list |
| `GridView` | 2D grid of rows and columns |
| `Spinner` | Dropdown selection menu |
| `Gallery` | Horizontally scrolling image gallery |

---

**5 Steps to Custom ListView Implementation**

**Step 1 — Activity Layout XML:**
```xml
<ListView
    android:id="@+id/listView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

**Step 2 — Row Layout XML:**
- Separate XML file defining the appearance of one list item
- Typically a `LinearLayout` with `ImageView` + `TextView`(s)
- ⚠️ If row contains focusable child elements (e.g. `CheckBox`, `Button`), add:
```xml
android:focusable="false"
```
on those child elements — otherwise row click listener won't fire

**Step 3 — Custom Adapter (extends `BaseAdapter`):**
```kotlin
class MyAdapter(
    private val context: Context,
    private val data: ArrayList<Student>,
    private val rowLayoutId: Int
) : BaseAdapter() {

    override fun getCount(): Int = data.size
    override fun getItem(position: Int): Any = data[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            // Inflate new view — expensive, only when no recycled view available
            view = LayoutInflater.from(context).inflate(rowLayoutId, parent, false)
            holder = ViewHolder()
            holder.textView = view.findViewById(R.id.textView)
            // ... find and store other views
            view.tag = holder
        } else {
            // Recycle existing view — cheap
            view = convertView
            holder = view.tag as ViewHolder
        }

        // Bind data to views
        val item = data[position]
        holder.textView?.text = item.name
        return view
    }

    // ViewHolder pattern — avoids repeated findViewById() calls
    private class ViewHolder {
        var textView: TextView? = null
        // ... other view references
    }
}
```

**Step 4 — Link Adapter to ListView:**
```kotlin
val listView = findViewById<ListView>(R.id.listView)
val adapter = MyAdapter(this, studentList, R.layout.row_layout)
listView.adapter = adapter
```

**Step 5 — Handle Item Clicks:**
```kotlin
listView.setOnItemClickListener { _, _, position, _ ->
    val item = studentList[position]
    // use item
}
```

---

**Simple ListView — ArrayAdapter from String Resources**

Define in `res/values/strings.xml`:
```xml
<string-array name="planets">
    <item>Mercury</item>
    <item>Venus</item>
    <item>Earth</item>
    <!-- ... -->
</string-array>
```

Create adapter in code:
```kotlin
adapter = ArrayAdapter.createFromResource(
    this,
    R.array.planets,
    android.R.layout.simple_list_item_1
)
listView.adapter = adapter
```
- `createFromResource()` parameters: `Context`, `R.array.yourArray`, layout resource
- Use `android.R.layout.simple_list_item_1` for simple single-text rows
- Supports localization — translate arrays in different language folders

---

## 5. Library Restrictions

Only the following libraries are approved. **Do not suggest or use alternatives.**

### Core Android / Jetpack
| Library | Purpose |
|---|---|
| `androidx.room:room-runtime` | Local SQLite database |
| `androidx.room:room-compiler` (kapt) | Room annotation processor |
| `androidx.room:room-ktx` | Kotlin extensions for Room |
| `androidx.swiperefreshlayout:swiperefreshlayout` | Pull-to-refresh gesture |
| `ViewModel` (Jetpack) | Lifecycle-aware UI data holder |
| `LiveData` (Jetpack) | Observable, lifecycle-aware data |
| `FragmentManager` / `FragmentTransaction` | Manual fragment management |
| `TabLayout` + `ViewPager` | Tab navigation (Material Design) |
| `androidx.navigation:navigation-fragment-ktx:2.7.6` | Navigation Component — fragment support |
| `androidx.navigation:navigation-ui-ktx:2.7.6` | Navigation Component — UI helpers |
| `androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3` | Safe Args — type-safe argument passing |
| `BottomNavigationView` (Material) | Bottom navigation bar |

### Networking
| Library | Purpose |
|---|---|
| `com.squareup.retrofit2:retrofit:2.11.0` | Type-safe HTTP client |
| `com.google.code.gson:gson:2.11.0` | JSON serialization/deserialization |
| `com.squareup.retrofit2:converter-gson:2.11.0` | Retrofit ↔ Gson bridge |

### Firebase
| Library | Purpose |
|---|---|
| `com.google.firebase:firebase-firestore-ktx` | Remote cloud database |
| `com.google.firebase:firebase-storage-ktx` | Image/file cloud storage |
| `com.google.firebase:firebase-auth-ktx` | User authentication |
| `FieldValue.serverTimestamp()` | Server-side timestamps |

### Image Loading
| Library | Purpose |
|---|---|
| **Picasso** — `com.squareup.picasso:picasso:2.8` | Loading images from URLs/URIs/Files with caching |

```kotlin
// build.gradle dependency
implementation 'com.squareup.picasso:picasso:2.8'
```

### Location
| Library | Purpose |
|---|---|
| `com.google.android.gms:play-services-location` | `FusedLocationProviderClient` — device GPS/network location |

```kotlin
// build.gradle dependency
implementation 'com.google.android.gms:play-services-location:21.0.1'
```

### Threading
| Library | Purpose |
|---|---|
| `java.util.concurrent.ExecutorService` | Background thread pool |
| `Executors.newFixedThreadPool(4)` | Fixed thread pool creation |
| `android.os.Handler` | Returning results to main thread |

### Camera / Gallery
| API | Purpose |
|---|---|
| `ActivityResultContracts.TakePicturePreview()` | Capture photo → returns `Bitmap` |
| `ActivityResultContracts.GetContent()` | Gallery image selection → returns `Uri` |
| `ActivityResultContracts.RequestMultiplePermissions()` | Request runtime permissions |
| `MediaStore.Images.Media.getBitmap()` | Convert gallery `Uri` → `Bitmap` |
| `FirebaseStorage.getInstance().reference` | Root Firebase Storage reference |
| `storageRef.putBytes(byteArray)` | Upload image bytes to Firebase Storage |
| `storageRef.downloadUrl` | Retrieve public download URL after upload |

---

## 6. Coding Style & Naming Conventions

### Variables
```kotlin
val name = "value"   // immutable — use by default
var name = "value"   // mutable — only when value must change
```
- Always prefer **type inference** — omit explicit types when Kotlin can infer them.

### Null Safety
```kotlin
val x: String? = null        // nullable declaration
val y = x?.length            // safe call operator
val z = x ?: "default"       // Elvis operator for fallback
val w = x!!.length           // !! only when certain non-null — use sparingly
```

### Classes
- No `new` keyword — instantiate as `val person = Person("John")`
- Classes are `final` by default — use `open` only when inheritance is required
- Use `data class` for model/entity classes
- Use `object` or `companion object` for singletons

### Click Listeners — Teacher's Style
**Preferred:** Lambda with `setOnClickListener`
```kotlin
val button = findViewById<Button>(R.id.button_id)
button.setOnClickListener {
    // handle click
}
```

**XML callback (acceptable for simple cases):**
```xml
android:onClick="sendMessage"
```
```kotlin
// Must be: public, return Unit, take exactly one View parameter
fun sendMessage(view: View) {
    // implementation
}
```

### Fragment Boilerplate — Teacher's Style
```kotlin
class ExampleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.example_fragment, container, false)
        // Always pass false as third argument to inflate()
    }
}
```

### Fragment Lifecycle Callbacks

| Callback | When Called | Typical Use |
|---|---|---|
| `onAttach()` | Fragment attached to Activity | Access Activity context |
| `onCreate()` | Fragment created | Register launchers (`registerForActivityResult`), init ViewModel |
| `onCreateView()` | Create fragment's UI | Inflate layout and return View |
| `onViewCreated()` | View fully created | Find views, set up click listeners, observe LiveData |
| `onActivityCreated()` | Activity's `onCreate` complete | Access Activity views if needed (deprecated — use `onViewCreated`) |
| `onStart()` | Fragment visible | Register receivers |
| `onResume()` | Fragment interactive | Resume animations |
| `onPause()` | Fragment losing focus | Pause operations, save state |
| `onStop()` | Fragment not visible | Detach listeners |
| `onDestroyView()` | View being destroyed | **Detach Firestore listeners, null out binding** |
| `onDestroy()` | Fragment destroyed | Final cleanup |
| `onDetach()` | Fragment detached from Activity | Release Activity reference |

> ⚠️ Detach Firestore listeners and null out ViewBinding in `onDestroyView()` — NOT `onDestroy()`.

### ViewModel — Surviving Configuration Changes

`ViewModel` survives device rotation and other configuration changes — **do not store UI references in it**.

```kotlin
// In Fragment — ViewModel persists across rotation:
private lateinit var viewModel: StudentsListViewModel

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ViewModelProvider returns the SAME instance after rotation
    viewModel = ViewModelProvider(this).get(StudentsListViewModel::class.java)
}
```

**What survives rotation (ViewModel):**
- `LiveData` values
- Data fetched from repository

**What does NOT survive rotation (must save manually):**
- UI state (scroll position, selected item, text field content)
- Use `onSaveInstanceState` + `onViewStateRestored` for these

### RecyclerView Adapter Inflation — Critical Rule
```java
// In onCreateViewHolder — always pass parent + false
View view = getLayoutInflater().inflate(
    R.layout.student_list_row,
    parent,
    false  // DO NOT attach to parent immediately
);
```

### ViewModel Retrieval in Fragment
```kotlin
viewModel = ViewModelProvider(this).get(StudentsListViewModel::class.java)
```

### LiveData Observation in Fragment
```kotlin
override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel.getData().observe(viewLifecycleOwner, { data ->
        // update UI — only called when Fragment is STARTED or RESUMED
    })
}
```
- Use `observeForever(observer)` for always-active observation (no lifecycle awareness)
- ⚠️ If using `observeForever`, you **must** manually call `removeObserver(observer)` to avoid leaks
- Observers are **automatically removed** when lifecycle reaches DESTROYED when using `observe(viewLifecycleOwner, ...)`

### Saving / Restoring Fragment State
```kotlin
override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    // save custom values into bundle
}

override fun onViewStateRestored(savedInstanceState: Bundle?) {
    super.onViewStateRestored(savedInstanceState)
    // restore custom values from bundle
}
```

### Scope Functions — When to Use Which
| Function | Ref | Returns | Use case |
|---|---|---|---|
| `let` | `it` | lambda result | null checks |
| `run` | `this` | lambda result | config + compute |
| `with` | `this` (param) | lambda result | grouping calls |
| `apply` | `this` | the object | object initialization |
| `also` | `it` | the object | side effects |

---

## 7. Common Patterns — "The Teacher's Way"

### 7.1 Room Database Setup

**Entity:**
```kotlin
@Entity
data class Student(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    var lastUpdated: Long?
)
```

**DAO:**
```kotlin
@Dao
interface StudentDao {
    @Query("SELECT * FROM Student")
    fun getAllStudents(): LiveData<List<Student>>  // return LiveData for reactive UI

    @Query("SELECT * FROM Student WHERE id =:id")
    fun getStudentById(id: String): Student

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg students: Student)

    @Update
    fun updateStudent(student: Student)

    @Delete
    fun delete(student: Student)

    @Query("DELETE FROM Student")
    fun deleteAll()
}
```

**Database class — Singleton with `lazy`:**
```kotlin
@Database(entities = [Student::class], version = 1)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}

object AppLocalDb {
    val db: AppLocalDbRepository by lazy {
        val context = MyApplication.Globals.appContext
            ?: throw IllegalStateException("Context not available")
        Room.databaseBuilder(
            context,
            AppLocalDbRepository::class.java,
            "dbFileName.db"
        ).fallbackToDestructiveMigration()
            .build()
    }
}
```

**Schema Export (add to `defaultConfig` in `build.gradle`):**
```groovy
defaultConfig {
    javaCompileOptions {
        annotationProcessorOptions {
            arguments = ["room.schemaLocation": "$projectDir/schemas".toString()]
        }
    }
}
```
- Generates a JSON file per DB version in `schemas/` for migration tracking and debugging
- Use `Room.inMemoryDatabaseBuilder()` instead of `databaseBuilder()` for **testing only**

### 7.2 Application Class Pattern (Required for Room context)

**AndroidManifest.xml:**
```xml
<application android:name=".base.MyApplication" ...>
```

**MyApplication.kt:**
```kotlin
class MyApplication : Application() {
    object Globals {
        var appContext: Context? = null
        var executorService: ExecutorService = Executors.newFixedThreadPool(4)
        var mainHandler: Handler = Handler(Looper.getMainLooper())
    }
    override fun onCreate() {
        super.onCreate()
        Globals.appContext = applicationContext
    }
}
```

### 7.3 Background Threading Pattern (Required for all DB operations)
```kotlin
// Execute on background thread
MyApplication.Globals.executorService.execute {
    val result = AppLocalDb.db.studentDao().getAllStudents()
    // Return to main thread for UI updates
    MyApplication.Globals.mainHandler.post {
        // update UI here
    }
}
```
> ❌ **Never run database operations on the main/UI thread** — causes crash.

### 7.4 Local + Remote Sync Strategy (Delta Sync)

Every record **must** have a `lastUpdated: Long?` field.

**Sync flow:**
1. Read `lastLocalUpdate` from **SharedPreferences**
2. Query Firebase: `orderByChild("lastUpdated").startAt(lastLocalUpdate)`
3. Insert/update results into local Room DB
4. Save new timestamp to SharedPreferences
5. Serve data to UI from local Room DB via LiveData

**Firebase timestamp on save** — defined in `ModelFirebase`, NOT in the entity:
```kotlin
// Inside ModelFirebase (not inside the @Entity class)
private fun postToMap(post: Post): Map<String, Any?> = mapOf(
    "id" to post.id,
    "name" to post.name,
    "imageUrl" to post.imageUrl,
    "lastUpdated" to FieldValue.serverTimestamp()  // always use server timestamp
)
```

**Firebase timestamp on read** — also in `ModelFirebase`:
```kotlin
private fun postFromMap(map: Map<String, Any?>): Post {
    val ts = map["lastUpdated"] as? Timestamp
    return Post(
        id = map["id"] as? String ?: "",
        name = map["name"] as? String ?: "",
        imageUrl = map["imageUrl"] as? String ?: "",
        lastUpdated = ts?.seconds ?: 0L
    )
}
```

> ⚠️ The `@Entity` data class stays **plain** (no `toMap`/`fromMap` methods inside it). Serialization logic lives in `ModelFirebase`. This matches the teacher's pattern.

### 7.5 LiveData + ViewModel Pattern

**ViewModel:**
```kotlin
class StudentsListViewModel : ViewModel() {
    private val data: LiveData<List<Student>> = StudentModel.instance.getAllStudents()
    fun getData(): LiveData<List<Student>> = data
}
```

**Advanced — `MutableLiveData` that manages Firebase listeners automatically:**
```kotlin
// Inner class inside Model
class StudentListData : MutableLiveData<List<Student>>() {
    override fun onActive() {
        super.onActive()
        modelFirebase.getAllStudents { students ->
            setValue(students)  // start listening when observers active
        }
    }
    override fun onInactive() {
        super.onInactive()
        modelFirebase.cancelGetAllStudents()  // stop listening when no observers
    }
    init {
        setValue(mutableListOf())
    }
}
```

### 7.6 Retrofit Networking Pattern

**Dependencies:**
```groovy
implementation 'com.squareup.retrofit2:retrofit:2.11.0'
implementation 'com.google.code.gson:gson:2.11.0'
implementation 'com.squareup.retrofit2:converter-gson:2.11.0'
```

**Model:**
```kotlin
data class Movie(
    @SerializedName("original_title") val originalTitle: String,
    // field names must match JSON keys, use @SerializedName for snake_case → camelCase
)
```

**API Interface:**
```kotlin
interface MovieApi {
    @GET("movie/top_rated")
    fun getTopRatedMovies(@Query("api_key") apiKey: String): Call<MovieResponse>
}
```

**Client (Singleton):**
```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.themoviedb.org/3/")  // must end with /
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(MovieApi::class.java)
```

**Making a call (always async with `enqueue`):**
```kotlin
api.getTopRatedMovies(apiKey).enqueue(object : Callback<MovieResponse> {
    override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
        // handle success
    }
    override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
        // handle failure
    }
})
```

**OkHttp Interceptor for global headers:**
```kotlin
val interceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
    chain.proceed(request)
}
val okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
```

### 7.7 RecyclerView Pattern

5 required components: `RecyclerView` → `LayoutManager` → `Adapter` → `ViewHolder` → Row XML

**Item click handling** (no built-in listener — must implement manually):
1. Define interface inside Adapter: `interface OnItemClickListener { fun onClick(position: Int) }`
2. Store listener reference in Adapter + provide setter
3. Adapter method checks null and calls `listener.onClick(position)`
4. Pass adapter reference to ViewHolder in `onCreateViewHolder`
5. ViewHolder sets click listener on root view, uses `adapterPosition`
6. Add ripple to row layout root: `android:background="?attr/selectableItemBackground"`

**Passing data to new Activity:**
```kotlin
val intent = Intent(applicationContext, NewActivity::class.java)
intent.putExtra("key_name", value)
startActivity(intent)
```

**Receiving data:**
```kotlin
val extras = intent.extras
if (extras != null) {
    val value = extras.getString("key_name")
}
```

### 7.8 Camera / Gallery Pattern

> ⚠️ Launchers **must** be declared as Fragment/Activity member variables — never inside a method.

**Step 1 — Declare launchers at class level:**
```kotlin
private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
private lateinit var galleryLauncher: ActivityResultLauncher<String>
private var selectedImageBitmap: Bitmap? = null
```

**Step 2 — Register launchers in `onCreate()` (before `onCreateView`):**
```kotlin
// Camera — returns a Bitmap thumbnail
cameraLauncher = registerForActivityResult(
    ActivityResultContracts.TakePicturePreview()
) { bitmap ->
    if (bitmap != null) {
        selectedImageBitmap = bitmap
        binding?.profileImageView?.setImageBitmap(bitmap)
    } else {
        Toast.makeText(requireContext(), "Failed to capture photo", Toast.LENGTH_SHORT).show()
    }
}

// Gallery — returns a Uri; load into ImageView using Picasso
galleryLauncher = registerForActivityResult(
    ActivityResultContracts.GetContent()
) { uri ->
    uri?.let {
        Picasso.get().load(uri).into(binding?.profileImageView)
        // If you also need a Bitmap from the Uri:
        selectedImageBitmap = MediaStore.Images.Media.getBitmap(
            requireActivity().contentResolver, uri
        )
    }
}
```

**Step 3 — Launch on button click:**
```kotlin
binding?.cameraButton?.setOnClickListener { cameraLauncher.launch(null) }
binding?.galleryButton?.setOnClickListener { galleryLauncher.launch("image/*") }
```

**Step 4 — Upload Bitmap to Firebase Storage:**
```kotlin
fun uploadImageToFirebase(bitmap: Bitmap, onSuccess: (String) -> Unit) {
    // Convert Bitmap → ByteArray
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
    val imageData = baos.toByteArray()

    // Upload to Firebase Storage
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

    imageRef.putBytes(imageData)
        .addOnSuccessListener {
            // Get public download URL
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())   // pass URL back to caller
            }
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Upload failed: ${e.message}")
        }
}
```

**Step 5 — Display image from URL with Picasso:**
```kotlin
// Load from URL into ImageView (handles caching automatically)
Picasso.get()
    .load(imageUrl)
    .placeholder(R.drawable.default_avatar)   // shown while loading
    .error(R.drawable.error_image)            // shown on failure
    .into(imageView)

// Load from Uri (e.g. gallery result)
Picasso.get().load(uri).into(imageView)

// Load from local file
Picasso.get().load(File(filePath)).into(imageView)
```

**Manifest permissions required:**
```xml
<uses-permission android:name="android.permission.CAMERA" />
<!-- READ_EXTERNAL_STORAGE required on API < 33 for gallery access -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

### 7.9 Swipe-to-Refresh Pattern

**Layout XML — wrap RecyclerView inside SwipeRefreshLayout:**
```xml
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/swipeRefresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

**Fragment code:**
```kotlin
binding?.swipeRefresh?.setOnRefreshListener {
    viewModel.refreshData()
}

// Observe loading state — ALWAYS set isRefreshing = false on both success AND error
viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
    binding?.swipeRefresh?.isRefreshing = isLoading
}
```

> ⚠️ Always call `isRefreshing = false` on **both success AND error** — otherwise the spinner spins forever.

### 7.10 GeoLocation / Proximity Search Pattern

#### Getting the Device Location — `FusedLocationProviderClient`

**Manifest permissions (declare both):**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

**Step 1 — Check & request runtime permissions:**
```kotlin
private val locationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                  permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
        fetchLocation()
    } else {
        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
    }
}

// Call this when you need location:
fun requestLocationPermission() {
    locationPermissionLauncher.launch(
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
}
```

**Step 2 — Fetch last known location (one-shot):**
```kotlin
private fun fetchLocation() {
    val fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    if (ActivityCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) return

    fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            val lat = location.latitude
            val lng = location.longitude
            // use coordinates here
        }
    }
}
```

**Step 3 — Continuous location updates:**
```kotlin
val locationRequest = LocationRequest.create().apply {
    interval = 10000          // update every 10 seconds
    fastestInterval = 5000    // but no faster than every 5 seconds
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
}

val locationCallback = object : LocationCallback() {
    override fun onLocationResult(result: LocationResult) {
        val location = result.lastLocation ?: return
        val lat = location.latitude
        val lng = location.longitude
        // update UI or save to Firestore
    }
}

// Start updates
fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

// Stop updates (in onStop() or onDestroyView())
fusedClient.removeLocationUpdates(locationCallback)
```

**Step 4 — Reverse Geocoding (coordinates → address):**
```kotlin
val geocoder = Geocoder(requireContext(), Locale.getDefault())
val addresses = geocoder.getFromLocation(lat, lng, 1)
if (!addresses.isNullOrEmpty()) {
    val address = addresses[0].getAddressLine(0)  // full street address
    val city = addresses[0].locality
    val country = addresses[0].countryName
}
```

---

#### GeoHash Proximity Search Pattern

**GeoHash Precision Table**
| Length | Approx Area | Typical Use Case |
|---|---|---|
| 4 | ~20km × 20km | City-level search |
| 5 | ~2.4km × 2.4km | Neighborhood search |
| 6 | ~600m × 600m | Nearby places |
| 7 | ~150m × 150m | Walking distance |
| 8 | ~38m × 19m | Building-level precision |

> Best practice: store geohashes at **multiple precision levels** (e.g. `hash6`, `hash7`) to optimize queries for different radii without recalculating.

**Firestore document structure:**
```
/collection/{doc_id}
  - geohash: "9q8yyk"         ← store at appropriate precision level
  - coordinates:
      - latitude: 37.7749
      - longitude: -122.4194
```

**Query pattern:**
```kotlin
val hashPrefix = GeoHash.encode(userLocation, 6) // length 6 = ~600m radius
firestore.collection("restaurants")
    .whereGreaterThanOrEqualTo("coordinates.geohash", hashPrefix)
    .whereLessThan("coordinates.geohash", hashPrefix + "\uf8ff")
    .get()
    .addOnSuccessListener { documents ->
        // Always post-filter by actual distance to handle boundary edge cases
        val nearby = documents.filter { doc -> calculateDistance(...) <= 600 }
    }
```

---

## 8. Navigation

The teacher covers **two approaches**. Use the Navigation Component (Section 8.1) for new screens. Manual `FragmentTransaction` (Section 8.2) is also shown and acceptable.

---

### 8.1 Navigation Component (Recommended)

**3 Core Components**
| Component | Role |
|---|---|
| **Navigation Graph** | XML resource — defines all destinations, actions, arguments |
| **NavHost** (`NavHostFragment`) | Empty container in Activity layout — displays current destination |
| **NavController** | Manages back stack and navigation between destinations |

---

**Dependencies (`app/build.gradle`):**
```kotlin
val nav_version = "2.7.6"
implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
```

**Safe Args plugin — top-level `build.gradle`:**
```groovy
buildscript {
    repositories { google() }
    dependencies {
        def nav_version = "2.5.3"
        classpath "androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version"
    }
}
```

**Apply Safe Args in `app/build.gradle`:**
```groovy
plugins {
    id 'com.android.application'
    id 'androidx.navigation.safeargs'   // enables type-safe argument passing
}
```

---

**NavHost in Activity Layout XML:**
```xml
<fragment
    android:id="@+id/main_navhost_frag"
    android:name="androidx.navigation.fragment.NavHostFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:defaultNavHost="true"
    app:navGraph="@navigation/nav_graph" />
```
- `app:defaultNavHost="true"` → intercepts system Back button (**only one NavHost per layout can have this**)
- `app:navGraph` → links to your navigation graph XML file

---

**Create Navigation Graph:**
1. Right-click `res/` → New → Android Resource File
2. Name: e.g. `nav_graph`, Resource type: `Navigation`
3. Opens in Navigation Editor — add destinations visually

**Destination Attributes:**
| Attribute | Description |
|---|---|
| `id` | Unique identifier used in code |
| `label` | User-facing name shown in action bar |
| `class` | Fully qualified Fragment/Activity class name |
| `type` | `fragment`, `activity`, or custom |

---

**Core Navigation Principles (teacher's rules):**
1. Every app has a **fixed start destination**
2. Navigation state = **stack of destinations** (current on top)
3. **Up and Back are equivalent** within the app's task
4. **Up never exits the app** — hide/disable at start destination
5. **Deep links build a proper back stack**

---

**Basic Navigation (by action ID):**
```kotlin
mButton.setOnClickListener { view ->
    Navigation.findNavController(view)
        .navigate(R.id.action_first_to_second)
}
```

**Simplified (no extra logic needed):**
```kotlin
btn.setOnClickListener(
    Navigation.createNavigateOnClickListener(R.id.action_startFragment_to_secondFragment)
)
```

---

**Navigating Backward:**
```kotlin
// Pop back to a specific destination (false = keep it in stack)
Navigation.findNavController(view).popBackStack(R.id.startFragment, false)
```

Or define pop behavior in nav graph XML:
```xml
<action
    android:id="@+id/action_back_home"
    app:destination="@id/homeFragment"
    app:popUpTo="@+id/homeFragment"
    app:popUpToInclusive="true" />
```
> ⚠️ `popUpToInclusive="true"` also removes the specified destination from the stack

---

**Passing Arguments — Safe Args (recommended):**

Define in nav graph:
```xml
<action
    android:id="@+id/startMyFragment"
    app:destination="@+id/myFragment">
    <argument
        android:name="myArg"
        app:argType="integer"
        android:defaultValue="1" />
</action>
```

Supported types: `integer`, `long`, `float`, `string`, `boolean`, resource reference, `Parcelable`, `Serializable`, arrays

Send (Safe Args generates `[FragmentName]Directions` class):
```kotlin
val action = StartFragmentDirections
    .actionStartFragmentToSecondFragment("123456")
btn.setOnClickListener(
    Navigation.createNavigateOnClickListener(action)
)
```

Receive (Safe Args generates `[FragmentName]Args` class):
```kotlin
class SecondFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_second, container, false)
        val args = SecondFragmentArgs.fromBundle(requireArguments())
        val studentId = args.studentId
        return v
    }
}
```
> After modifying the nav graph, **rebuild project** to regenerate Safe Args classes

---

**Global Actions** (accessible from any destination):
```kotlin
// In nav graph: right-click destination → Add Action → Global
val action = StartFragmentDirections
    .actionGlobalThirdFragment("first arg", 1234)
btn.setOnClickListener(
    Navigation.createNavigateOnClickListener(action)
)
```

---

**NavigationUI — Action Bar Integration:**
```kotlin
// In Activity onCreate:
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
        navController.navigateUp()
    }
    return super.onOptionsItemSelected(item)
}
```

**NavigationUI — Menu items auto-navigate (IDs must match destination IDs):**
```kotlin
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        android.R.id.home -> navController.navigateUp()
        else -> NavigationUI.onNavDestinationSelected(item, navController)
    }
}
```

Menu XML — use `@id/` (not `@+id/`) to reference existing destination IDs:
```xml
<item
    android:id="@id/secondFragment"
    android:icon="@drawable/ic_launcher_foreground"
    android:title="second"
    app:showAsAction="ifRoom" />
```

---

**Activity Menu Methods:**
```kotlin
// Inflate menu (called once on activity create):
override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.game_menu, menu)
    return true
}

// Handle item selection:
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.new_game -> { newGame(); true }
        else -> super.onOptionsItemSelected(item)
    }
}
```

**Fragment Menu Override:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)   // required to enable menu in fragment
}

override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    menu.clear()   // remove activity menu items (omit to ADD instead of replace)
    inflater.inflate(R.menu.add_fragment_menu, menu)
    super.onCreateOptionsMenu(menu, inflater)
}
```

---

**Bottom Navigation Setup:**

Layout XML:
```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bottomNavigationView"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:menu="@menu/bottom_nav" />
```

Bottom nav menu XML (`res/menu/bottom_nav.xml`) — IDs **must match destination IDs**:
```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/secondFragment" android:title="second" />
    <item android:id="@+id/thirdFragment"  android:title="third" />
    <item android:id="@+id/startFragment"  android:title="first" />
</menu>
```

Connect to NavController in Activity `onCreate`:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val navController = findNavController(R.id.main_navhost_frag)
    val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
    NavigationUI.setupWithNavController(bottomNav, navController)
}
```
- Use **3–5 items** in bottom navigation
- Bottom nav items do **not** stack on top of each other — they replace the current fragment
- For **more than 5 destinations** → use a **Navigation Drawer** instead

**Navigation Drawer:**
- Use Android Studio's **"Navigation Drawer Activity"** template
- Verify Safe Args is configured after using template
- Connect menu item IDs to destination IDs (same pattern as bottom nav)

---

### 8.2 Manual Fragment Transactions (also shown in slides)

```kotlin
// Add fragment programmatically
val fragmentTransaction = supportFragmentManager.beginTransaction()
val fragment = ExampleFragment()
fragmentTransaction.add(R.id.fragment_container, fragment)
fragmentTransaction.commit()
// Use commitAllowingStateLoss() if called after onSaveInstanceState()
```

**Back stack (manual):**
- Only added to back stack when `addToBackStack()` is explicitly called
- Without it → Back button destroys the entire host Activity
- Programmatic back: `fragmentManager.popBackStack()`
- **Home/Up button** override:
```kotlin
supportActionBar?.setDisplayHomeAsUpEnabled(true)

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        android.R.id.home -> { switchToListFragment(); true }
        else -> super.onOptionsItemSelected(item)
    }
}
```

**Finding a fragment reference:**
```kotlin
val frag = supportFragmentManager.findFragmentById(R.id.my_fragment) as MyFragment?
```

**Tab navigation (manual):**
- `TabLayout` + `ViewPager` with `FragmentPagerAdapter`
- Use `BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT`
- ❌ `ActionBar` tabs are deprecated — use `TabLayout`

---

## 9. Firebase Cloud Firestore & Authentication (Lecture — Online Persistency)

### 9.1 Initial Setup

```kotlin
// Get a Firestore instance (do this once, e.g. in your Model/Repository)
val db = FirebaseFirestore.getInstance()
```

- Create your Firebase project at **www.firebase.com**
- Add the `google-services.json` to your `app/` folder
- Firebase Firestore includes **offline caching** — enable/disable based on app requirements

---

### 9.2 Security Rules (Firebase Console)

Before any read/write can happen, configure rules in the Firebase console.

**Development mode — unrestricted access (⚠️ NOT for production):**
```
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write;
    }
  }
}
```
> For production: implement proper authentication and authorization rules.

---

### 9.3 Data Structure — Documents & Collections

**Document:**
- The fundamental unit of data in Firestore
- Lightweight record containing fields that map to values
- Stored in **JSON format** (strings, numbers, booleans, arrays, nested objects, timestamps)
- Can contain **nested objects (maps)** for complex hierarchical data

**Collection:**
- A container that holds multiple documents
- Identified by a name (string) — use descriptive plural names: `"users"`, `"products"`, `"orders"`
- Can contain any number of documents

**Subcollections (nested collections):**
- Documents can contain their own collections
- Use cases: user profiles with a `posts` subcollection, products with a `reviews` subcollection, orders with an `items` subcollection
- Each subcollection is independent and can be queried separately

---

### 9.4 Custom Object Requirements

For Firestore to automatically serialize/deserialize your Kotlin classes:

| Requirement | Details |
|---|---|
| **Public no-arg constructor** | Required so Firestore can instantiate the object when reading |
| **Public getters** | Each property to be stored must have a public getter |

```kotlin
data class City(
    val name: String = "",
    val state: String = "",
    val country: String = "",
    val capital: Boolean = false,
    val population: Long = 0,
    val regions: List<String> = listOf()
)
```
> ⚠️ Default values on all fields are required for the no-arg constructor in Kotlin data classes.

---

### 9.5 Writing Data

**Three approaches:**

| Method | When to Use |
|---|---|
| `set()` with explicit ID | You define the document ID (e.g. username, product SKU) |
| `add()` with auto-generated ID | Let Firestore generate a unique ID — use for posts, comments, messages |
| Create empty doc then populate | When you need the generated ID before adding content |

**Set with explicit ID (creates or fully overwrites):**
```kotlin
val city = City("Los Angeles", "CA", "USA", false, 5000000L, listOf("west_coast", "socal"))
db.collection("cities").document("LA").set(city)
    .addOnSuccessListener {
        // write succeeded — update UI here
    }
    .addOnFailureListener { e ->
        // handle error
    }
```
> ⚠️ `set()` **completely overwrites** the existing document. Use merge options to avoid this.

**Add with auto-generated ID:**
```kotlin
db.collection("cities").add(data)
    .addOnSuccessListener { documentRef ->
        Log.d(TAG, "Added with ID: ${documentRef.id}")
    }
    .addOnFailureListener { e ->
        // handle error
    }
```

**Create empty document first (get ID before populating):**
```kotlin
val newDocRef = db.collection("cities").document() // generates ID immediately
val generatedId = newDocRef.id
newDocRef.set(data)
```

---

### 9.6 Updating Documents

**Partial update — only modify specific fields (other fields untouched):**
```kotlin
db.collection("cities").document("LA")
    .update("population", 6000000L)
    .addOnSuccessListener { /* success */ }
    .addOnFailureListener { /* error */ }
```
> ⚠️ `update()` fails if the document does not exist. Use `set()` with merge if you want to create-or-update.

**Update multiple fields at once:**
```kotlin
db.collection("cities").document("LA")
    .update(
        mapOf(
            "name" to "Los Angeles",
            "population" to 6000000L
        )
    )
```

**Update nested objects using dot notation:**
```kotlin
// Only updates the nested field — all other nested fields remain intact
db.collection("users").document("uid123")
    .update("address.city", "Tel Aviv")
```

---

### 9.7 Server-Side Timestamps

Always use server timestamps rather than client-generated timestamps — ensures consistency across all clients regardless of local clock:

```kotlin
db.collection("events").document("event1")
    .update("lastUpdated", FieldValue.serverTimestamp())
```
> `FieldValue.serverTimestamp()` — Firestore uses the **server's current time**, eliminating client clock / timezone issues.

---

### 9.8 Deleting Documents

```kotlin
db.collection("cities").document("LA").delete()
    .addOnSuccessListener { /* deleted */ }
    .addOnFailureListener { /* error */ }
```
> ⚠️ Deleting a document does **NOT** delete its subcollections. Delete subcollection documents separately.

---

### 9.9 Reading Data

**Read a single document:**
```kotlin
db.collection("cities").document("LA").get()
    .addOnSuccessListener { document ->
        if (document.exists()) {
            // Manual field access
            val name = document.getString("name")

            // OR convert to custom object (preferred)
            val city = document.toObject(City::class.java)
        }
    }
    .addOnFailureListener { e ->
        // handle error
    }
```

**Read all documents in a collection (with optional filter):**
```kotlin
db.collection("cities")
    .whereEqualTo("country", "USA")   // optional filter
    .get()
    .addOnSuccessListener { result ->
        for (document in result) {
            val city = document.toObject(City::class.java)
        }
    }
    .addOnFailureListener { e ->
        // handle error
    }
```

---

### 9.10 Real-Time Listeners

**Listen to a single document (fires immediately + on every change):**
```kotlin
val listenerRegistration = db.collection("cities").document("LA")
    .addSnapshotListener { snapshot, error ->
        if (error != null) {
            // handle error
            return@addSnapshotListener
        }
        if (snapshot != null && snapshot.exists()) {
            val city = snapshot.toObject(City::class.java)
            // update UI
        }
    }
```

**Listen to an entire collection or query result:**
```kotlin
val listenerRegistration = db.collection("cities")
    .whereEqualTo("country", "USA")
    .addSnapshotListener { snapshots, error ->
        if (error != null) return@addSnapshotListener
        for (document in snapshots!!) {
            val city = document.toObject(City::class.java)
        }
    }
```

**Detect only specific changes (ADDED / MODIFIED / REMOVED):**
```kotlin
db.collection("cities").addSnapshotListener { snapshots, error ->
    if (error != null) return@addSnapshotListener
    for (change in snapshots!!.documentChanges) {
        when (change.type) {
            DocumentChange.Type.ADDED    -> { /* new document */ }
            DocumentChange.Type.MODIFIED -> { /* updated document */ }
            DocumentChange.Type.REMOVED  -> { /* deleted document */ }
        }
    }
}
```
> Use `documentChanges` to update your UI efficiently — only process what changed, not the full collection.

---

### 9.11 Detaching Listeners (Cleanup)

Active listeners consume **network bandwidth** and can cause **memory leaks**.

```kotlin
// Store the registration when you attach
private var listenerRegistration: ListenerRegistration? = null

listenerRegistration = db.collection("cities").addSnapshotListener { ... }

// Detach when no longer needed
listenerRegistration?.remove()
```

**When to detach:**

| Component | Lifecycle method to detach in |
|---|---|
| Activity | `onStop()` |
| Fragment | `onDestroyView()` |

---

### 9.12 Firebase Authentication

#### Setup
Enable desired auth methods in the Firebase console → **Authentication** section.

#### Register a New User
```kotlin
FirebaseAuth.getInstance()
    .createUserWithEmailAndPassword(email, password)
    .addOnSuccessListener { result ->
        val user = result.user  // FirebaseUser
        // navigate to main screen
    }
    .addOnFailureListener { e ->
        // show error to user
    }
```
> Firebase handles password hashing, storage, and security automatically.

#### Sign In
```kotlin
FirebaseAuth.getInstance()
    .signInWithEmailAndPassword(email, password)
    .addOnSuccessListener { result ->
        val user = result.user
        // auth state persists across sessions until explicit sign-out
    }
    .addOnFailureListener { e ->
        // show error (wrong credentials, etc.)
    }
```

#### Check Current Auth State
```kotlin
val currentUser = FirebaseAuth.getInstance().currentUser
if (currentUser != null) {
    // user is logged in
    val uid = currentUser.uid
    val email = currentUser.email
} else {
    // no user logged in — redirect to login screen
}
```

#### Sign Out
```kotlin
FirebaseAuth.getInstance().signOut()
// After this: currentUser == null
// User must sign in again to access authenticated features
```

**Sign-out best practices:**
1. Clear any cached user data from local storage
2. Navigate to the login or home screen
3. Stop any active Firestore listeners (`listenerRegistration?.remove()`)
4. Provide clear feedback to the user

---

## 10. Things NOT Covered in Lectures

> ⚠️ The following require lecturer approval before use:
- Jetpack Compose
- Hilt / Dagger (dependency injection)
- Coroutines / Flow (slides use `ExecutorService` + `Handler`)
- Glide (slides use **Picasso**)
- DataBinding (slides use `findViewById`)
- Any library not listed in Section 5

---

## 11. Final Project Requirements (Mandatory Checklist)

> Source: Official final project guidelines by the course staff.
> Every item below is **mandatory** — failure to implement = failing grade.

### 11.1 Threshold Requirements (Pass/Fail)

| # | Requirement |
|---|---|
| 1 | **Sharing** — one user uploads content (text + image), another user can see it |
| 2 | **External REST API** — display content fetched from an external REST API |

> ❌ Instagram-clone apps are **not allowed**.

---

### 11.2 Grading Criteria

#### Completeness & UX
- The app tells a complete, logical story — everything that should work, works
- Correct app design and **reasonable UI using Material Design** (as demonstrated in class)

#### Code Structure
- **MVVM** — modular, no code duplication, clean and short functions
- **Must use:** `ViewModel`, `LiveData`, `Room`
- **Must use:** Navigation Component with **Nav Graph** (`graph nav`)
- **Must use:** Google Design Guidelines as taught in class

#### Network
- **No synchronous network calls**
- **Spinners** must appear in the right places during loading

#### Data Management
- **Local cache** (Room/SQLite) + **remote storage** (Firebase)
- **Gradual loading** (paginated / lazy)
- Cache both **images** (Picasso) and **objects** (Room)
- ❌ **Must NOT use Firebase features for local storage** — use Room only
- ✅ Picasso is allowed **for images only**

#### Navigation & Fragments
- Use **Fragments**, **Nav Graph**, pass parameters via **SafeArgs**

#### User Management (Authentication)
- Users must **register** (Firebase Authentication)
- Each user can view their own posts **in a separate screen**
- Users can **edit their posts** — both text and image
- Users can **delete** their own posts
- **Auto-login** — if the user was previously logged in, the app opens directly to the main screen (no re-login)
- Users can **logout**
- **Profile screen** — shows profile image and name; must support **editing name and profile image**
- (Deleting a user account is NOT required)

#### Git
- **Git is mandatory** from the start — not just uploading at the end

---

### 11.3 Group Size: 3+ Members — Additional Requirements

| Requirement | Detail |
|---|---|
| **Map** | A map screen displaying all shared content |
| **Location-linked data** | All shared content must be associated with a location |
| **GPS** | Use device GPS to get current location |
| **Map tap → detail screen** | Tapping an item on the map opens its detail screen |

---

### 11.4 Project Submission Stages

| Stage | Deliverables |
|---|---|
| **1 — Definition** | App goal, functional requirements (must-have / should-have / future) |
| **2 — Design** | Use-case story (user's perspective) + mockup (must match use-case) |
| **3 — Submission** | Working app + code structure description + code handoff for testing |

> ⚠️ **No second chances at the defence** — if you show up, the app is tested as-is. No fixes after submission.


























