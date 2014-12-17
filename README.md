LightJson is a simple lightweight Java library for working with JSON is designed to be light, fast, simple and easy to use.

**Using LightJson**

1. Define a bean

```java
@JsonObject
public class Bean {
	@JsonField
	private Field field;

	public Field getField() { return field; }
	public void setField(Field field) { this.field = field; }
}
```
_OR_
```java
@JsonObject(AutomaticBinding = true)
public class Bean {
    private Field field;

    public Field getField() { return field; }
    public void setField(Field field) { this.field = field; }
}
```

Field can be one of:
* Primitive type (int, long, ...)
* Number (Integer, Long, ...)
* String
* Array ([])
* Collection (List<>)
* Map
* Another Class annotated as @JsonObject 

_**NOTE:** Unmarshalling Collections of Collections, Maps of Maps, Collections of Maps, etc is not supported by LightJson because of Java Generics limitations._

2. Marshal to JSON

```java
Bean bean = new Bean();
bean.setField(field);
String testJson = LightJson.to(bean);
System.out.println("marshal result: " + testJson);
```

You will get :
> marshal result: {"field":"field value"}

3. Unmarshal from JSON

```java
Bean uBean = LightJson.from(testJson, Bean.class);
field = uBean.getField();
```

You will get field as it was before marshalling.
