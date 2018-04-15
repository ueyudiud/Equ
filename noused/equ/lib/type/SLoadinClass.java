/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.type;

import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Optional;
import java.util.function.Consumer;

import org.objectweb.asm.Type;

import equ.lib.base.SCodeWriter;
import equ.lib.base.SDeclearation;
import equ.lib.base.SGenericDeclearation;
import equ.lib.base.SMethodWriter;
import equ.lib.func.SFunc;
import equ.lib.func.SFuncs;
import equ.lib.func.SGenericFunc;
import equ.lib.func.STypeMethod;
import equ.runtime.VariableType;
import equ.util.Arguments;
import equ.util.GS;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
class SLoadinClass<T> implements SClass
{
	final CLTypeLoader loader;
	final Class<T> clazz;
	
	public SLoadinClass(CLTypeLoader loader, Class<T> clazz)
	{
		this.loader = loader;
		this.clazz = clazz;
	}
	
	@Override
	public String getName()
	{
		return this.clazz.getCanonicalName();
	}
	
	@Override
	public String getDescriptor()
	{
		return Type.getDescriptor(this.clazz);
	}
	
	@Override
	public boolean convert(SType type)
	{
		if (type instanceof SWildcardType)
		{
			return convert(((SWildcardType) type).getLowerBounds());
		}
		else if (type instanceof SLoadinClass<?>)
		{
			return ((SLoadinClass<?>) type).clazz.isAssignableFrom(this.clazz);
		}
		else if (type instanceof SParameterizedType)
		{
			if (type.getRawClass() instanceof SLoadinClass<?>)
			{
				return ((SLoadinClass<?>) type.getRawClass()).clazz.isAssignableFrom(this.clazz);
			}
			else return false;//Array or Pointer
		}
		return type == SSpecialClasses.ANY || type == SSpecialClasses.ANY_REF;
	}
	
	@Override
	public SClassLoader getTypeLoader()
	{
		return this.loader;
	}
	
	@Override
	public boolean isInterface()
	{
		return this.clazz.isInterface();
	}
	
	private SType[] interfaces;
	
	@Override
	public SType[] getGenericInterfaces()
	{
		if (this.interfaces == null)
		{
			java.lang.reflect.Type[] types = this.clazz.getGenericInterfaces();
			this.interfaces = new SType[types.length];
			for (int i = 0; i < this.interfaces.length; ++i)
			{
				this.interfaces[i] = this.loader.getTypeOf(types[i]);
			}
		}
		return this.interfaces;
	}
	
	@Override
	public SClass getSupertype()
	{
		return this.loader.getSupertypeOf(this);
	}
	
	@Override
	public SType getGenericSupertypes()
	{
		return this.clazz.getGenericSuperclass() == null ? SSpecialClasses.ANY_REF :
			this.loader.getTypeOf(this.clazz.getGenericSuperclass());
	}
	
	private SType[] members;
	
	@Override
	public SType[] getMemberTypes()
	{
		if (this.members == null)
		{
			this.members = GS.transform(this.clazz.getDeclaredClasses(), this.loader::loadClassUnsafe, SType.class);
		}
		return this.members;
	}
	
	@Override
	public SType provideType(SType... parameters)
	{
		Arguments.checkLength(this.clazz.getTypeParameters().length, parameters, "Illegal type parameters count.");
		if (this.clazz.getTypeParameters().length == 0)
			return this;
		if (this.clazz.isMemberClass() && !Modifier.isStatic(this.clazz.getModifiers()))
			return this.loader.loadParameterizedType(
					this.loader.loadClassUnsafe(this.clazz.getEnclosingClass()), this, parameters);
		return this.loader.loadParameterizedType(null, this, parameters);
	}
	
	private SLoadinTypeParameters<SLoadinClass<T>, Class<T>>[] typeParameters;
	private SLoadinMethod[] methods;
	private SLoadinConstructor<T>[] constructors;
	
	@Override
	public SVariableType<?>[] getTypeParameters()
	{
		if (this.typeParameters == null)
		{
			this.typeParameters = GS.transform(this.clazz.getTypeParameters(), v->new SLoadinTypeParameters(this.loader, this, v), SLoadinTypeParameters.class);
		}
		return this.typeParameters;
	}
	
	@Override
	public SLoadinMethod[] getFunctions()
	{
		if (this.methods == null)
		{
			this.methods = GS.transform(this.clazz.getDeclaredMethods(), m->new SLoadinMethod(this, m), SLoadinMethod.class);
		}
		return this.methods;
	}
	
	@Override
	public SLoadinConstructor<T>[] getConstructors()
	{
		if (this.constructors == null)
		{
			this.constructors = GS.transform(this.clazz.getDeclaredConstructors(), c->new SLoadinConstructor(this, c), SLoadinConstructor.class);
		}
		return this.constructors;
	}
	
	private Optional<SDeclearation> owner;
	
	@Override
	public SDeclearation getOwner()
	{
		if (this.owner == null)
		{
			if (this.clazz.isMemberClass())
			{
				if (this.clazz.getEnclosingConstructor() != null)
				{
					final Constructor<?> constructor = this.clazz.getEnclosingConstructor();
					this.owner = Optional.of(GS.of(getConstructors(), c->c.constructor == constructor));
				}
				else if (this.clazz.getEnclosingMethod() != null)
				{
					final Method method = this.clazz.getEnclosingMethod();
					this.owner = Optional.of(GS.of(getFunctions(), c->c.function == method));
				}
				else
				{
					this.owner = Optional.of(this.loader.loadClass(this.clazz.getEnclosingClass()));
				}
			}
			else
			{
				this.owner = Optional.empty();
			}
		}
		return this.owner.orElse(null);
	}
	
	@Override
	public String toString()
	{
		return "Loadin { " + this.clazz.getSimpleName() + " }";
	}
}

class SLoadinMethod implements SGenericFunc
{
	Method function;
	SLoadinClass<?> declearation;
	
	SType[] parameters;
	SVariableType<?>[] typeParameters;
	SType result;
	
	SLoadinMethod(SLoadinClass<?> declearation, Method function)
	{
		this.declearation = declearation;
		this.function = function;
	}
	
	@Override
	public String getName()
	{
		return this.function.getName();
	}
	
	@Override
	public SVariableType<?>[] getTypeParameters()
	{
		if (this.typeParameters == null)
		{
			this.typeParameters = GS.transform(this.function.getTypeParameters(),
					v->new SLoadinTypeParameters(this.declearation.loader, this, v), SVariableType.class);
		}
		return this.typeParameters;
	}
	
	@Override
	public SType getOwner()
	{
		return this.declearation;
	}
	
	@Override
	public STypeMethod getMethodType()
	{
		return SFuncs.toMethodType(this.declearation.getTypeLoader(), new SType[][] {getParameters()}, getResult(), getTypeParameters());
	}
	
	@Override
	public SType[] getParameters()
	{
		if (this.parameters == null)
		{
			this.parameters = GS.transform(this.function.getGenericParameterTypes(),
					this.declearation.loader::getTypeOf, SType.class);
		}
		return this.parameters;
	}
	
	@Override
	public SType getResult()
	{
		return this.result == null ?
				(this.result = this.declearation.loader.getTypeOf(this.function.getGenericReturnType())) :
					this.result;
	}
	
	@Override
	public SMethodWriter writer()
	{
		return (needResult, writer, parameters) -> {
			for (Consumer<SCodeWriter> consumer : parameters)
				consumer.accept(writer);
			writer.visitor().visitMethodInsn(
					(Modifier.isStatic(SLoadinMethod.this.function.getModifiers()) ? INVOKESTATIC : INVOKEVIRTUAL),
					Type.getInternalName(SLoadinMethod.this.function.getDeclaringClass()),
					SLoadinMethod.this.function.getName(),
					Type.getMethodDescriptor(SLoadinMethod.this.function),
					SLoadinMethod.this.function.isDefault());
			if (!needResult)
			{
				Class<?> result = this.function.getReturnType();
				if (result == long.class || result == double.class)
					writer.visitor().visitInsn(POP2);
				else if (result != void.class)
					writer.visitor().visitInsn(POP);
			}
			
		};
	}
	
	@Override
	public String toString()
	{
		return "Loadin { " + getResult().getName() + " " + this.function.getName() + "(" + Strings.toString(getParameters(), SType::getName)  + ") }";
	}
}

class SLoadinConstructor<T> implements SGenericFunc
{
	SLoadinClass<T> declearation;
	Constructor<T> constructor;
	
	SLoadinConstructor(SLoadinClass<T> declearation, Constructor<T> constructor)
	{
		this.declearation = declearation;
		this.constructor = constructor;
	}
	
	SType[] parameters;
	SVariableType<?>[] typeParameters;
	
	@Override
	public String getName()
	{
		return this.constructor.getName();
	}
	
	@Override
	public SVariableType<?>[] getTypeParameters()
	{
		if (this.typeParameters == null)
		{
			this.typeParameters = GS.transform(this.constructor.getTypeParameters(),
					v->new SLoadinTypeParameters(this.declearation.loader, this, v), SVariableType.class);
		}
		return this.typeParameters;
	}
	
	@Override
	public SType getOwner()
	{
		return this.declearation;
	}
	
	@Override
	public STypeMethod getMethodType()
	{
		return SFuncs.toMethodType(this.declearation.loader, new SType[][] {getParameters()}, getResult(), getTypeParameters());
	}
	
	@Override
	public SType[] getParameters()
	{
		if (this.parameters == null)
		{
			this.parameters = GS.transform(this.constructor.getGenericParameterTypes(),
					this.declearation.loader::getTypeOf, SType.class);
		}
		return this.parameters;
	}
	
	@Override
	public SType getResult()
	{
		return this.declearation;
	}
	
	@Override
	public SMethodWriter writer()
	{
		return (needResult, writer, parameters) -> {
			writer.visitor().visitTypeInsn(NEW, Type.getInternalName(this.constructor.getDeclaringClass()));
			if (needResult)
				writer.visitor().visitInsn(DUP);
			for (Consumer<SCodeWriter> consumer : parameters)
				consumer.accept(writer);
			writer.visitor().visitMethodInsn(
					INVOKESPECIAL,
					Type.getInternalName(this.constructor.getDeclaringClass()),
					"<init>",
					Type.getConstructorDescriptor(this.constructor),
					false);
		};
	}
	
	@Override
	public String toString()
	{
		return "Loadin { " + this.declearation.getName() + " " + this.constructor.getName() + "(" + Strings.toString(getParameters(), SType::getName)  + ") }";
	}
}

class SLoadinTypeParameters<D extends SGenericDeclearation, S extends GenericDeclaration>
implements SVariableType<D>
{
	final CLTypeLoader loader;
	D source;
	TypeVariable<S> variable;
	
	private SFunc[] functions;
	private SType[] bounds;
	
	public SLoadinTypeParameters(CLTypeLoader loader, D source, TypeVariable<S> variable)
	{
		this.loader = loader;
		this.source = source;
		this.variable = variable;
	}
	
	@Override
	public SClassLoader getTypeLoader()
	{
		return this.loader;
	}
	
	@Override
	public String getName()
	{
		return this.variable.getName();
	}
	
	private EnumVariableType variableType;
	
	@Override
	public EnumVariableType getVariableType()
	{
		if (this.variableType == null)
		{
			VariableType type = this.variable.getAnnotation(VariableType.class);
			this.variableType = type == null ? EnumVariableType.NORMAL : type.value();
		}
		return this.variableType;
	}
	
	@Override
	public SType[] getMemberTypes()
	{
		return new SType[0];
	}
	
	@Override
	public SFunc[] getFunctions()
	{
		if (this.functions == null)
		{
			//TODO
		}
		return this.functions;
	}
	
	@Override
	public SFunc[] getConstructors()
	{
		return EMPTY;
	}
	
	@Override
	public D getGenericDeclaration()
	{
		return this.source;
	}
	
	@Override
	public SType[] getBounds()
	{
		if (this.bounds == null)
		{
			this.bounds = GS.transform(this.variable.getBounds(), this.loader::getTypeOf, SType.class);
		}
		return this.bounds;
	}
	
	@Override
	public String toString()
	{
		return "Loadin { " + this.variable.getName() + " }";
	}
}